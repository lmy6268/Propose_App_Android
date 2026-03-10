package com.hanadulset.pro_poseapp.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CompDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.PoseDataSource
import com.hanadulset.pro_poseapp.data.datasource.interfaces.TrackingDataSource
import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.repository.useMat
import com.hanadulset.pro_poseapp.domain.model.PoseResultEntity
import com.hanadulset.pro_poseapp.data.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val compDataSource: CompDataSource,
    private val poseDataSource: PoseDataSource,
    private val trackingDataSource: TrackingDataSource,
    private val imageRepository: ImageRepository
) : AIRepository {

    private val emptyPoseResultEntity by lazy {
        PoseResultEntity(
            poseDataList = listOf(),
            backgroundAngleList = listOf(),
            backgroundId = -1
        )
    }

    // 모델 및 데이터 초기화
    override suspend fun preRunModel() = withContext(Dispatchers.IO) {
        compDataSource.loadModel()
        poseDataSource.initPoseData()
    }

    // 포즈 분석 수행 (내부에서 최신 프레임 획득)
    override suspend fun requestPoseAnalysis(): PoseResultEntity {
        imageRepository.startCapture()
        try {
            // 캡처 활성화 후 프레임이 도착할 때까지 대기
            var frame: Any? = null
            var retryCount = 0
            while (frame == null && retryCount < 10) {
                frame = imageRepository.acquireFrame()
                if (frame == null) {
                    delay(50)
                    retryCount++
                }
            }
            if (frame == null) return emptyPoseResultEntity
            
            // acquireFrame으로 받은 프레임(Mat)의 소유권을 가져와서 사용 후 자동 해제
            return frame.useMat(imageRepository) { poseDataSource.recommendPose(it as Mat).toDomain() }
        } finally {
            imageRepository.stopCapture()
        }
    }

    // 구도 권장/추적 수행 (실시간 Flow 반환)
    override suspend fun analyzeComposition(): Flow<Pair<Float, Float>?> {
        return flow {
            imageRepository.startCapture()
            try {
                // 1. 초기 구도 정보 획득을 위해 최신 프레임 Pull (재시도 로직 포함)
                var initialFrame: Any? = null
                var retryCount = 0
                val maxRetries = 10
                while (initialFrame == null && retryCount < maxRetries) {
                    delay(100)
                    retryCount++
                    initialFrame = imageRepository.acquireFrame()
                }
                
                if (initialFrame == null) {
                    Log.e("AIRepository", "초기 프레임을 가져올 수 없습니다. 구도 분석을 건너뜁니다.")
                    emit(null)
                    return@flow
                }
                
                val trackingMat = initialFrame as? Mat
                if (trackingMat == null) {
                    Log.e("AIRepository", "이미지가 Mat이 아닙니다. 구도 분석을 건너뜁니다.")
                    imageRepository.releaseMat(initialFrame)
                    emit(null)
                    return@flow
                }
                
                // 2. 초기 구도 정보 획득
                val initialComp = compDataSource.recommendCompData(trackingMat)

                // 3. 트래킹 시작 (Mat과 초기 구도 좌표 전달)
                trackingDataSource.startTracking(trackingMat, initialComp)
                emit(initialComp)
                
                // 트래킹에 사용된 초기 프레임 해제
                trackingMat.release()

                // 4. 최신 프레임을 지속적으로 Pull 하여 트래킹 업데이트 수행
                try {
                    while (currentCoroutineContext().isActive) {
                        val frame = imageRepository.acquireFrame()
                        frame?.useMat(imageRepository) { m ->
                            val mat = m as Mat
                            if (!mat.empty()) {
                                trackingDataSource.processFrame(mat)
                                emit(trackingDataSource.trackingFlow.value)
                            }
                        }
                        // 약 16ms 주기로 분석 수행
                        delay(16)
                    }
                } finally {
                    // Flow 종료 시 트래킹 리소스 해제
                    trackingDataSource.stopTracking()
                }
            } finally {
                imageRepository.stopCapture()
            }
        }
    }
}

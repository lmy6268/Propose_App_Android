package com.hanadulset.pro_poseapp.data.repository

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
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

    // 구도 권장/추적 수행 (TrackingDataSource가 Flow를 직접 생성·반환)
    override suspend fun analyzeComposition(): Flow<Pair<Float, Float>?> {
        imageRepository.startCapture()

        val initialFrame = acquireInitialFrame()
        if (initialFrame == null) {
            Log.e("AIRepository", "초기 프레임을 가져올 수 없습니다.")
            imageRepository.stopCapture()
            return flowOf(null)
        }

        val initialComp = compDataSource.recommendCompData(initialFrame)

        return trackingDataSource.setTracker(
            initialMat = initialFrame,
            initialOffset = initialComp,
            frameProvider = { imageRepository.acquireFrame() as? Mat }
        ).onCompletion {
            initialFrame.release()
            imageRepository.stopCapture()
        }
    }

    /** 초기 프레임을 최대 10회 재시도하여 획득 */
    private suspend fun acquireInitialFrame(): Mat? {
        repeat(10) {
            delay(100)
            val frame = imageRepository.acquireFrame()
            if (frame is Mat && !frame.empty()) return frame
            (frame as? Mat)?.release()
        }
        return null
    }
}

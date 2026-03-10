package com.hanadulset.pro_poseapp.domain.repository
import com.hanadulset.pro_poseapp.domain.model.ImageResultEntity
import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    // 프레임 수신 알림 (CameraX 콜백)
    fun onFrameReceived(image: Any)

    // 프레임 캡처 활성화/비활성화 (분석 요청 시에만 활성화)
    fun startCapture()
    fun stopCapture()

    // 최신 프레임을 꺼내서 소유권을 호출자에게 전달 (useMat {} 블록으로 자동 해제)
    fun acquireFrame(): Any?

    // 사진 캡처 및 저장 (단일 작업)
    suspend fun processCapturedPhoto(image: Any, eventData: CaptureEventEntity): String

    suspend fun loadAllCapturedImages(): List<ImageResultEntity>

    // 특정 캡처 이미지 삭제
    suspend fun deleteCapturedImage(uri: String): Boolean

    // 고정 화면을 위한 가이드 라인(테두리) 이미지 생성 (단일 작업)
    suspend fun <T> getFixedImage(imageOrMat: Any): T

    // 썸네일 표시 등을 위한 마지막 저장 이미지의 URI 획득
    suspend fun getLastSavedImageUri(): String?

    /** 이미지/Mat 핸들링 관련 기능 (공통) **/
    
    // 이미지/URI/Proxy 객체를 AI 분석이 가능한 Mat 핸들(Any)로 변환
    suspend fun convertToMat(image: Any): Any?
    
    // Mat 핸들 자원 해제
    fun releaseMat(mat: Any)
}

/**
 * 도메인 레이어에서 Mat과 같은 저수준 자원을 안전하게 관리하기 위한 확장 함수.
 * block 실행 후 예외 발생 여부와 상관없이 자동으로 Mat 자원을 해제합니다.
 */
inline fun <R> Any.useMat(repository: ImageRepository, block: (Any) -> R): R {
    return try {
        block(this)
    } finally {
        repository.releaseMat(this)
    }
}
package com.hanadulset.pro_poseapp.domain.usecase.image

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

/**
 * 카메라로부터 수신된 실시간 이미지 프레임을 처리하기 위한 UseCase입니다.
 * 수신된 프레임을 ImageRepository로 전달하여 전역적으로 활용 가능하게 합니다.
 */
class ProcessImageFrameUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(image: Any) {
        imageRepository.onFrameReceived(image)
    }
}

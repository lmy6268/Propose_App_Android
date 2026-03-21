package com.hanadulset.pro_poseapp.domain.usecase.image

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class GetLatestImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * 비즈니스 로직: 갤러리 또는 저장소에서 마지막으로 저장된 이미지의 URI를 획득합니다.
     */
    suspend operator fun invoke(): String? {
        return imageRepository.getLastSavedImageUri()
    }
}
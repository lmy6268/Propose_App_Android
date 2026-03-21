package com.hanadulset.pro_poseapp.domain.usecase.ai

import com.hanadulset.pro_poseapp.domain.repository.AIRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.repository.useMat
import javax.inject.Inject

class GetPoseFromImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val aiRepository: AIRepository
) {
    /**
     * 비즈니스 로직: URI -> Mat 변환 -> 포즈 분석 -> 결과 반환 절차를 조율합니다.
     */
    @Suppress("UNCHECKED_CAST")
    suspend operator fun <T> invoke(uri: String): T {
        val matHandle = imageRepository.convertToMat(uri)
            ?: throw IllegalArgumentException("Failed to load image from URI: $uri")
            
        return matHandle.useMat(imageRepository) { handle ->
             imageRepository.getFixedImage(handle)
        }
    }
}

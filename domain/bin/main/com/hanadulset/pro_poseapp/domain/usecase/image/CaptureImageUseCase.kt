package com.hanadulset.pro_poseapp.domain.usecase.image

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity
import javax.inject.Inject

class CaptureImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(image: Any, captureEventData: CaptureEventEntity): String {
        return imageRepository.processCapturedPhoto(image, captureEventData)
    }
}
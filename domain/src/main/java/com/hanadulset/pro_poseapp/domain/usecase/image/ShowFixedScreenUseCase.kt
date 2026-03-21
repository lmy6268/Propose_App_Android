package com.hanadulset.pro_poseapp.domain.usecase.image

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class ShowFixedScreenUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun <T> invoke(): T? {
        imageRepository.startCapture()
        try {
            var frame: Any? = null
            var retryCount = 0
            while (frame == null && retryCount < 10) {
                frame = imageRepository.acquireFrame()
                if (frame == null) {
                    kotlinx.coroutines.delay(50)
                    retryCount++
                }
            }
            if (frame == null) return null
            return imageRepository.getFixedImage(frame)
        } finally {
            imageRepository.stopCapture()
        }
    }
}
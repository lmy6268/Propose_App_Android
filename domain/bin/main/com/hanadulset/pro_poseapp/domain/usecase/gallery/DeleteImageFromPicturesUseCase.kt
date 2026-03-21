package com.hanadulset.pro_poseapp.domain.usecase.gallery

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class DeleteImageFromPicturesUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uri: String): Boolean {
        return imageRepository.deleteCapturedImage(uri)
    }
}

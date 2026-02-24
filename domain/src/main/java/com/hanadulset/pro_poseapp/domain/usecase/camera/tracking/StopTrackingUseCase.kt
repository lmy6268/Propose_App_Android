package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class StopTrackingUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    operator fun invoke() = imageRepository.stopPointOffset()

}
package com.hanadulset.pro_poseapp.domain.usecase.camera

import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeImage
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class ShowFixedScreenUseCase
@Inject
constructor(
        private val imageRepository: ImageRepository,
) {
    suspend operator fun invoke(backgroundImage: ProposeImage) =
            imageRepository.getFixedScreen(backgroundImage)
}

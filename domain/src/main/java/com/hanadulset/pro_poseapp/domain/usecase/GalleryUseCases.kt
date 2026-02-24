package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.gallery.*
import javax.inject.Inject

data class GalleryUseCases
@Inject
constructor(val loadAllProposeImage: GetImagesFromPicturesUseCase,
            val deleteImage: DeleteImageFromPicturesUseCase)

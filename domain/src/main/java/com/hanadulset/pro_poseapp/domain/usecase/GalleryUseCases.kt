package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.gallery.DeleteImageFromPicturesUseCase
import com.hanadulset.pro_poseapp.domain.usecase.gallery.GetImagesFromPicturesUseCase
import javax.inject.Inject

data class GalleryUseCases @Inject constructor(
    val getImagesFromPicturesUseCase: GetImagesFromPicturesUseCase,
    val deleteImageFromPicturesUseCase: DeleteImageFromPicturesUseCase
)

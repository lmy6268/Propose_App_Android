package com.hanadulset.pro_poseapp.domain.usecase.camera


import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeAnalyzer
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeLifecycleOwner
import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeSurfaceProvider
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject

class BindCameraUseCase @Inject constructor(
    private val repository: CameraRepository,
) {
    suspend operator fun invoke(
        lifecycleOwner: ProposeLifecycleOwner,
        surfaceProvider: ProposeSurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ProposeAnalyzer,

        ) = repository.bindCamera(
        lifecycleOwner,
        surfaceProvider,
        aspectRatio,
        previewRotation,
        analyzer
    )
}
package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.camera.*
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.StartTrackingUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.StopTrackingUseCase
import javax.inject.Inject

data class CameraUseCases
@Inject
constructor(
        val bindCamera: BindCameraUseCase,
        val captureProposeImage: CaptureImageUseCase,
        val getLatestProposeImage: GetLatestImageUseCase,
        val setFocus: SetFocusUseCase,
        val setZoomLevel: SetZoomLevelUseCase,
        val showFixedScreen: ShowFixedScreenUseCase,
        val unbindCamera: UnbindCameraUseCase,
        val startTracking: StartTrackingUseCase,
        val stopTracking: StopTrackingUseCase
)

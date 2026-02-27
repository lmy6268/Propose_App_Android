package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.usecase.camera.*
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.StopPointOffsetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.UpdatePointOffsetUseCase
import javax.inject.Inject

data class CameraUseCases @Inject constructor(
    val bindCameraUseCase: BindCameraUseCase,
    val unbindCameraUseCase: UnbindCameraUseCase,
    val captureImageUseCase: CaptureImageUseCase,
    val getLatestImageUseCase: GetLatestImageUseCase,
    val setFocusUseCase: SetFocusUseCase,
    val setZoomLevelUseCase: SetZoomLevelUseCase,
    val showFixedScreenUseCase: ShowFixedScreenUseCase,
    val updatePointOffsetUseCase: UpdatePointOffsetUseCase,
    val stopPointOffsetUseCase: StopPointOffsetUseCase
)

package com.hanadulset.pro_poseapp.presentation.feature.camera.model

import android.util.Size

data class CameraState(
    val cameraStateId: Int,
    val exception: Exception? = null,
    val exceptionMessage: String? = null,
    val imageAnalyzerResolution: Size? = null
) {
    companion object {
        const val CAMERA_INIT_ERROR = -1
        const val CAMERA_INIT_COMPLETE = 0
        const val CAMERA_INIT_ON_PROCESS = 1
        const val CAMERA_INIT_NOTHING = 2
    }
}
package com.hanadulset.pro_poseapp.domain.model.camera

data class CameraStateModel(
        val cameraStateId: Int,
        val exception: Exception? = null,
        val exceptionMessage: String? = null,
        val imageAnalyzerResolution: CameraResolutionModel? = null
) {
    companion object {
        const val CAMERA_INIT_ERROR = -1
        const val CAMERA_INIT_COMPLETE = 0
        const val CAMERA_INIT_ON_PROCESS = 1
        const val CAMERA_INIT_NOTHING = 2
    }
}

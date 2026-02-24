package com.hanadulset.pro_poseapp.domain.repository

import com.hanadulset.pro_poseapp.domain.model.wrapper.ProposeUri
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.domain.model.camera.CameraStateModel
import com.hanadulset.pro_poseapp.ui.utils.eventlog.CaptureEventData


//카메라 기능을 담당하는 레포지토리
interface CameraRepository {
    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ProposeImageAnalysis.Analyzer,
    ): CameraStateModel

    suspend fun takePhoto(eventData: CaptureEventData): ProposeUri
    fun setZoomRatio(zoomLevel: Float)
    fun sendCameraSound()
    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long)
    fun unbindCameraResource(): Boolean
}
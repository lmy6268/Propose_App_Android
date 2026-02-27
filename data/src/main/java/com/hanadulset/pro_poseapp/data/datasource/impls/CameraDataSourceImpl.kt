package com.hanadulset.pro_poseapp.data.datasource.impls

import android.content.Context
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.data.datasource.interfaces.CameraDataSource
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CameraDataSource {
    private lateinit var preview: Preview
    private var imageCapture: ImageCapture? = null
    private lateinit var imageAnalysis: ImageAnalysis
    
    private val analysisExecutor = Dispatchers.Default.asExecutor()
    private val mainExecutor = ContextCompat.getMainExecutor(context)

    private lateinit var camera: Camera
    private lateinit var cameraProvider: ProcessCameraProvider

    override suspend fun initCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: Analyzer
    ): CameraState = suspendCancellableCoroutine { cont ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, surfaceProvider, previewRotation, aspectRatio, analyzer)
                cont.resume(CameraState(CAMERA_INIT_COMPLETE, imageAnalyzerResolution = imageAnalysis.resolutionInfo?.resolution))
            } catch (e: Exception) {
                cont.resume(CameraState(CAMERA_INIT_ERROR, e, e.message))
            }
        }, mainExecutor)
    }

    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        previewRotation: Int,
        aspectRatio: Int,
        analyzer: Analyzer
    ) {
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy(aspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO))
            .build()

        preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(previewRotation)
            .build()
            .apply { setSurfaceProvider(surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(previewRotation)
            .build()

        imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(previewRotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply { setAnalyzer(analysisExecutor, analyzer) }

        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis)
    }

    override suspend fun takePhoto() = suspendCancellableCoroutine { cont ->
        imageCapture?.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                cont.resume(image)
            }
            override fun onError(exception: ImageCaptureException) {
                cont.resumeWithException(exception)
            }
        }) ?: cont.resumeWithException(Exception("ImageCapture is not initialized"))
    }

    override fun unbindCameraResources(): Boolean {
        return try {
            if (::cameraProvider.isInitialized) cameraProvider.unbindAll()
            true
        } catch (_: Exception) { false }
    }

    override fun setZoomLevel(zoomLevel: Float) {
        if (::camera.isInitialized) camera.cameraControl.setZoomRatio(zoomLevel)
    }

    override fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        if (::camera.isInitialized) {
            camera.cameraControl.startFocusAndMetering(
                FocusMeteringAction.Builder(meteringPoint)
                    .setAutoCancelDuration(durationMilliSeconds, TimeUnit.MILLISECONDS)
                    .build()
            )
        }
    }

    companion object {
        const val CAMERA_INIT_COMPLETE = 0
        const val CAMERA_INIT_ERROR = 1
    }
}

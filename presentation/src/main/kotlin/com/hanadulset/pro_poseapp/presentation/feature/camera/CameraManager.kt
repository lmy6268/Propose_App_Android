package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.content.Context
import android.media.AudioManager
import android.media.MediaActionSound
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.CameraState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val analysisExecutor = Dispatchers.Default.asExecutor()

    private val shutterSoundManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer
    ): CameraState = withContext(Dispatchers.Main) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val provider = cameraProviderFuture.await()
            cameraProvider = provider

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(
                    AspectRatioStrategy(
                        aspectRatio,
                        AspectRatioStrategy.FALLBACK_RULE_AUTO
                    )
                )
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

            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            CameraState(CameraState.CAMERA_INIT_COMPLETE, imageAnalyzerResolution = imageAnalysis?.resolutionInfo?.resolution)
        } catch (e: Exception) {
            CameraState(CameraState.CAMERA_INIT_ERROR, e, e.message)
        }
    }

    fun unbind() {
        cameraProvider?.unbindAll()
        camera = null
        preview = null
        imageCapture = null
        imageAnalysis = null
    }

    fun setZoom(zoomLevel: Float) {
        camera?.cameraControl?.setZoomRatio(zoomLevel)
    }

    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        camera?.cameraControl?.startFocusAndMetering(
            FocusMeteringAction.Builder(meteringPoint)
                .setAutoCancelDuration(durationMilliSeconds, TimeUnit.MILLISECONDS)
                .build()
        )
    }

    suspend fun takePhoto(): ImageProxy = suspendCancellableCoroutine { cont ->
        imageCapture?.takePicture(analysisExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                cont.resume(image)
            }

            override fun onError(exception: ImageCaptureException) {
                cont.resumeWithException(exception)
            }
        }) ?: cont.resumeWithException(Exception("ImageCapture is not initialized"))
    }

    fun sendShutterSound() {
        shutterSoundManager.setStreamVolume(
            AudioManager.STREAM_SYSTEM,
            1,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
        MediaActionSound().apply {
            play(MediaActionSound.SHUTTER_CLICK)
        }
    }
}

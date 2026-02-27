package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.AiUseCases
import com.hanadulset.pro_poseapp.domain.usecase.CameraUseCases
import com.hanadulset.pro_poseapp.domain.usecase.GalleryUseCases
import com.hanadulset.pro_poseapp.domain.usecase.UserUseCases
import com.hanadulset.pro_poseapp.utils.ImageUtils
import com.hanadulset.pro_poseapp.utils.UserSet
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.eventlog.CaptureEventData
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalGetImage
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraUseCases: CameraUseCases,
    private val aiUseCases: AiUseCases,
    private val userUseCases: UserUseCases,
    private val galleryUseCases: GalleryUseCases
) : ViewModel() {

    private val _trackingSwitchON = MutableStateFlow(false)
    private val _trackingTrigger = Channel<Unit>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            _trackingTrigger.receiveAsFlow().conflate().collect {
                if (_trackingSwitchON.value && _modifiedPointState.value != null) {
                    val backgroundBitmap = _bitmapState.value ?: return@collect
                    //이미지 사용하기
                    val analyzedImageSize = Size(backgroundBitmap.width, backgroundBitmap.height)
                    val res = cameraUseCases.updatePointOffsetUseCase(
                        targetOffset = convertAnalyzedOffsetToPreviewOffset(
                            reversed = true, offset = SizeF(
                                _modifiedPointState.value!!.x, _modifiedPointState.value!!.y
                            ), analyzedImageSize = analyzedImageSize
                        ), backgroundBitmap = backgroundBitmap
                    )
                    if (res == null) {
                        stopToTrack() //만약 에러인 경우 추적을 그만함.
                    } else {
                        _modifiedPointState.update {
                            convertAnalyzedOffsetToPreviewOffset(
                                false, res, analyzedImageSize = analyzedImageSize
                            ).let { Offset(it.width, it.height) }
                        }
                    }
                }
            }
        }
    }

    private val viewRateList = listOf(
        ViewRate(
            name = "4:3", aspectRatioType = AspectRatio.RATIO_4_3, aspectRatioSize = Size(3, 4)
        ), ViewRate(
            "16:9", aspectRatioType = AspectRatio.RATIO_16_9, aspectRatioSize = Size(9, 16)
        )
    )
    private val _userSetState = MutableStateFlow<UserSet?>(null)
    val userSetState = _userSetState.asStateFlow()


    private val _backgroundDataState = MutableStateFlow<Pair<Int, List<Double>>?>(null)
    val backgroundDataState = _backgroundDataState.asStateFlow()

    private val _aspectRatioState = MutableStateFlow(viewRateList[0])
    val aspectRatioState = _aspectRatioState.asStateFlow()


    private val _previewState = MutableStateFlow(CameraState(CameraState.CAMERA_INIT_NOTHING))


    private val _capturedBitmapState = MutableStateFlow<Uri?>(null)
    private val _poseResultState = MutableStateFlow<MutableList<PoseData>?>(null)
    private val _fixedScreenState = MutableStateFlow<Bitmap?>(null)
    private val _modifiedPointState = MutableStateFlow<Offset?>(null)

    val pointOffsetState = _modifiedPointState.asStateFlow()


    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()


    val fixedScreenState = _fixedScreenState.asStateFlow()
    val previewState = _previewState.asStateFlow()
    private val _bitmapState = MutableStateFlow<Bitmap?>(null)
    private val _bitmapDemandNow = MutableStateFlow(false)


    private var previewSizeState: androidx.compose.ui.geometry.Size? = null
    private val _poseOnRecommend = MutableStateFlow(false)


    private val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        imageProxy.use {
            _bitmapState.value = ImageUtils.imageToBitmap(it.image!!, it.imageInfo.rotationDegrees)
            trackToNewOffset()
        }
    }


    private fun convertAnalyzedOffsetToPreviewOffset(
        reversed: Boolean, offset: SizeF, analyzedImageSize: Size
    ): SizeF {
        return if (reversed)
            offset.let {
                SizeF(
                    (it.width / previewSizeState!!.width) * analyzedImageSize.width,
                    (it.height / previewSizeState!!.height) * analyzedImageSize.height
                )
            } else offset.let {
            SizeF(
                (it.width / analyzedImageSize.width) * previewSizeState!!.width,
                (it.height / analyzedImageSize.height) * previewSizeState!!.height
            )
        }
    }


    fun bindCameraToLifeCycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        previewRotation: Int
    ) {
        _previewState.value =
            CameraState(cameraStateId = CameraState.CAMERA_INIT_ON_PROCESS)
        viewModelScope.launch {
            val res = cameraUseCases.bindCameraUseCase(
                lifecycleOwner,
                surfaceProvider,
                aspectRatio = aspectRatioState.value.aspectRatioType,
                analyzer = imageAnalyzer,
                previewRotation = previewRotation
            )
            _previewState.value = res
        }
    }

    fun getPhoto(captureEventData: CaptureEventData) {
        viewModelScope.launch {
            _capturedBitmapState.value = cameraUseCases.captureImageUseCase(captureEventData)
        }
    }

    fun getViewRateList() = viewRateList

    fun reqPoseRecommend() {
        if (_poseOnRecommend.value.not()) {
            _poseOnRecommend.value = true
            _poseResultState.value = null
            if (_bitmapDemandNow.value.not()) _bitmapDemandNow.value = true
            viewModelScope.launch {
                _bitmapState.value?.let { bitmap ->
                    val recommendedData = aiUseCases.recommendPoseUseCase(bitmap)
                    _poseResultState.update {
                        recommendedData.poseDataList.apply {
                            add(0, PoseData(poseId = -1, -1))
                        }.subList(
                            0,
                            if (_userSetState.value != null) _userSetState.value!!.poseCnt + 1
                            else recommendedData.poseDataList.size
                        )
                    }
                    _backgroundDataState.update {
                        recommendedData.let { Pair(it.backgroundId, it.backgroundAngleList) }
                    }

                    _poseOnRecommend.value = false
                }
            }
        }
    }


    private fun trackToNewOffset() {
        if (_trackingSwitchON.value && _modifiedPointState.value != null) {
            _trackingTrigger.trySend(Unit)
        }
    }


    fun startToTrack(previewSize: androidx.compose.ui.geometry.Size) {
        previewSizeState = previewSize
        _trackingSwitchON.value = true
        viewModelScope.launch {
            _bitmapState.value?.let { bitmap ->
                aiUseCases.recommendCompInfoUseCase(bitmap).let { res ->
                    _modifiedPointState.update {
                        previewSizeState!!.center.let {
                            Offset(
                                it.x * ((1F + res.first * 2)),
                                it.y * ((1F + res.second * 2))
                            )
                        }
                    }

                }
            }
        }
    }

    fun stopToTrack() {
        _trackingSwitchON.update { false }
        _modifiedPointState.update { null }
        cameraUseCases.stopPointOffsetUseCase()
    }

    fun setZoomLevel(zoomLevel: Float) = cameraUseCases.setZoomLevelUseCase(zoomLevel)

    fun changeViewRate(idx: Int): Boolean {
        val res = _aspectRatioState.value.aspectRatioType == viewRateList[idx].aspectRatioType
        if (res.not()) _aspectRatioState.value = viewRateList[idx]
        return res

    }

    fun controlFixedScreen(isRequest: Boolean) {
        if (isRequest) {
            viewModelScope.launch {
                _bitmapState.value?.let { backgroundBitmap ->
                    _fixedScreenState.value = cameraUseCases.showFixedScreenUseCase(
                        backgroundBitmap = backgroundBitmap
                    )
                }
            }
        } else _fixedScreenState.value = null

    }

    fun getPoseFromImage(uri: Uri) {
        viewModelScope.launch {
            _fixedScreenState.value = null
            val res = aiUseCases.getPoseFromImageUseCase(uri)
            _fixedScreenState.value = res
            Log.d("따오기 이미지: ", uri.toString())
            galleryUseCases.deleteImageFromPicturesUseCase(uri)
        }
    }


    fun getLastImage() {
        viewModelScope.launch {
            _capturedBitmapState.value = cameraUseCases.getLatestImageUseCase()
        }
    }

    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        cameraUseCases.setFocusUseCase(meteringPoint, durationMilliSeconds)
    }


    fun loadUserSet() {
        viewModelScope.launch {
            _userSetState.update { userUseCases.loadUserSetUseCase() }
        }
    }

    fun saveUserSet(userSet: UserSet) {
        viewModelScope.launch {
            userUseCases.saveUserSetUseCase(userSet = userSet)
        }
    }

}

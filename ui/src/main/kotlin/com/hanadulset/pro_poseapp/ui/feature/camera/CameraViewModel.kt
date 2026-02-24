package com.hanadulset.pro_poseapp.ui.feature.camera

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
import com.hanadulset.pro_poseapp.domain.model.UserSetModel
import com.hanadulset.pro_poseapp.domain.model.camera.CameraStateModel
import com.hanadulset.pro_poseapp.domain.model.pose.PoseDataModel
import com.hanadulset.pro_poseapp.domain.usecase.AiUseCases
import com.hanadulset.pro_poseapp.domain.usecase.CameraUseCases
import com.hanadulset.pro_poseapp.domain.usecase.GalleryUseCases
import com.hanadulset.pro_poseapp.domain.usecase.UserUseCases
import com.hanadulset.pro_poseapp.ui.mapper.toBitmap
import com.hanadulset.pro_poseapp.ui.mapper.toDomainWrapper
import com.hanadulset.pro_poseapp.ui.mapper.toUri
import com.hanadulset.pro_poseapp.ui.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.ui.utils.eventlog.eventlog.CaptureEventData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ExperimentalGetImage
@HiltViewModel
class CameraViewModel
@Inject
constructor(
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
                    // 이미지 사용하기
                    val analyzedImageSize = Size(backgroundBitmap.width, backgroundBitmap.height)
                    val res =
                            cameraUseCases.startTracking(
                                    targetOffset =
                                            convertAnalyzedOffsetToPreviewOffset(
                                                    reversed = true,
                                                    offset =
                                                            SizeF(
                                                                    _modifiedPointState.value!!.x,
                                                                    _modifiedPointState.value!!.y
                                                            ),
                                                    analyzedImageSize = analyzedImageSize
                                            ),
                                    backgroundBitmap = backgroundBitmap
                            )
                    if (res == null) {
                        stopToTrack() // 만약 에러인 경우 추적을 그만함.
                    } else {
                        _modifiedPointState.update {
                            convertAnalyzedOffsetToPreviewOffset(
                                            false,
                                            res,
                                            analyzedImageSize = analyzedImageSize
                                    )
                                    .let { Offset(it.width, it.height) }
                        }
                    }
                }
            }
        }
    }

    private val viewRateList =
            listOf(
                    ViewRate(
                            name = "4:3",
                            aspectRatioType = AspectRatio.RATIO_4_3,
                            aspectRatioSize = Size(3, 4)
                    ),
                    ViewRate(
                            "16:9",
                            aspectRatioType = AspectRatio.RATIO_16_9,
                            aspectRatioSize = Size(9, 16)
                    )
            )
    private val _userSetState = MutableStateFlow<UserSetModel?>(null)

    val userSetState = _userSetState.asStateFlow()

    private val _backgroundDataState = MutableStateFlow<Pair<Int, List<Double>>?>(null)
    val backgroundDataState = _backgroundDataState.asStateFlow()

    private val _aspectRatioState = MutableStateFlow(viewRateList[0])
    val aspectRatioState = _aspectRatioState.asStateFlow()

    private val _previewState =
            MutableStateFlow(CameraStateModel(CameraStateModel.CAMERA_INIT_NOTHING))

    private val _capturedBitmapState =
            MutableStateFlow<Uri?>( // 캡쳐된 이미지 상태
                    null
            )
    private val _poseResultState = MutableStateFlow<MutableList<PoseDataModel>?>(null)
    private val _fixedScreenState = MutableStateFlow<Bitmap?>(null)
    private val _modifiedPointState = MutableStateFlow<Offset?>(null)

    val pointOffsetState = _modifiedPointState.asStateFlow()

    // State Getter

    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()

    val fixedScreenState = _fixedScreenState.asStateFlow()
    val previewState = _previewState.asStateFlow()
    private val _bitmapState = MutableStateFlow<Bitmap?>(null)
    private val _bitmapDemandNow = MutableStateFlow(false)

    private var previewSizeState: androidx.compose.ui.geometry.Size? = null
    private val _poseOnRecommend = MutableStateFlow(false)

    // 매 프레임의 image를 수신함.
    private val imageAnalyzer =
            ImageAnalysis.Analyzer { imageProxy ->
                imageProxy.use {
                    _bitmapState.value = imageToBitmap(it.image!!, it.imageInfo.rotationDegrees)
                    trackToNewOffset()
                }
            }

    private fun convertAnalyzedOffsetToPreviewOffset(
            reversed: Boolean,
            offset: SizeF,
            analyzedImageSize: Size
    ): SizeF {
        return if (reversed) // preview -> analyzed
                offset.let {
                    SizeF(
                            (it.width / previewSizeState!!.width) * analyzedImageSize.width,
                            (it.height / previewSizeState!!.height) * analyzedImageSize.height
                    )
                }
        else
                offset.let { // analyzed -> preview
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
                CameraStateModel(
                        cameraStateId = CameraStateModel.CAMERA_INIT_ON_PROCESS
                ) // OnProgress
        viewModelScope.launch {
            val res =
                    cameraUseCases.bindCamera(
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
            _capturedBitmapState.value =
                    cameraUseCases.captureProposeImage(captureEventData).toUri()
        }
    }

    fun getViewRateList() = viewRateList

    fun reqPoseRecommend() {
        if (_poseOnRecommend.value.not()) {
            _poseOnRecommend.value = true // 포즈 추천이 시작됨을 알림
            _poseResultState.value = null
            if (_bitmapDemandNow.value.not()) _bitmapDemandNow.value = true
            viewModelScope.launch {
                _bitmapState.value?.let { bitmap ->
                    val recommendedData = aiUseCases.recommendPose(bitmap.toDomainWrapper())
                    _poseResultState.update {
                        recommendedData
                                .poseDataList
                                .apply { add(0, PoseDataModel(poseId = -1, -1)) }
                                .subList(
                                        0,
                                        if (_userSetState.value != null)
                                                _userSetState.value!!.poseCnt + 1
                                        else recommendedData.poseDataList.size
                                )
                    }
                    _backgroundDataState.update {
                        recommendedData.let { Pair(it.backgroundId, it.backgroundAngleList) }
                    }

                    _poseOnRecommend.value = false
                    // 포즈 추천이 끝남을 알림
                }
            }
        }
    }

    private fun trackToNewOffset() {
        // 구도 추천 로직
        if (_trackingSwitchON.value && _modifiedPointState.value != null) {
            _trackingTrigger.trySend(Unit)
        }
    }

    fun startToTrack(previewSize: androidx.compose.ui.geometry.Size) {
        previewSizeState = previewSize
        _trackingSwitchON.value = true
        viewModelScope.launch {
            _bitmapState.value?.let { bitmap ->
                aiUseCases.recommendCompInfo(bitmap.toDomainWrapper()).let { res ->
                    _modifiedPointState.update {
                        previewSizeState!!.center.let {
                            Offset(it.x * ((1F + res.first * 2)), it.y * ((1F + res.second * 2)))
                        }
                    }
                }
            }
        }
    }

    fun stopToTrack() {
        _trackingSwitchON.update { false }
        _modifiedPointState.update { null }
        cameraUseCases.stopTracking()
    }

    fun setZoomLevel(zoomLevel: Float) = cameraUseCases.setZoomLevel(zoomLevel)

    fun changeViewRate(idx: Int): Boolean {
        val res = _aspectRatioState.value.aspectRatioType == viewRateList[idx].aspectRatioType
        if (res.not()) _aspectRatioState.value = viewRateList[idx]
        return res
    }

    fun controlFixedScreen(isRequest: Boolean) {
        if (isRequest) {
            viewModelScope.launch {
                _bitmapState.value?.let { backgroundBitmap ->
                    _fixedScreenState.value =
                            cameraUseCases
                                    .showFixedScreen(
                                            backgroundImage = backgroundBitmap.toDomainWrapper()
                                    )
                                    .toBitmap()
                }
            }
        } else _fixedScreenState.value = null
    }

    fun getPoseFromImage(uri: Uri) {
        viewModelScope.launch {
            _fixedScreenState.value = null
            val res = aiUseCases.getPoseFromProposeImage(uri.toDomainWrapper())?.toBitmap()
            _fixedScreenState.value = res
            Log.d("따오기 이미지: ", uri.toString())
            galleryUseCases.deleteImage(uri.toDomainWrapper())
        }
    }

    // 최근 이미지 불러오기
    fun getLastImage() {
        viewModelScope.launch {
            _capturedBitmapState.value = cameraUseCases.getLatestProposeImage()?.toUri()
        }
    }

    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        cameraUseCases.setFocus(meteringPoint, durationMilliSeconds)
    }

    fun loadUserSet() {
        viewModelScope.launch { _userSetState.update { userUseCases.loadUserSet() } }
    }

    fun saveUserSet(userSet: UserSetModel) {
        viewModelScope.launch { userUseCases.saveUserSet(userSet = userSet) }
    }
}

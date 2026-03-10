package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.AiUseCases
import com.hanadulset.pro_poseapp.domain.usecase.GalleryUseCases
import com.hanadulset.pro_poseapp.domain.usecase.ImageUseCases
import com.hanadulset.pro_poseapp.domain.usecase.UserUseCases
import com.hanadulset.pro_poseapp.domain.model.UserEntity
import com.hanadulset.pro_poseapp.domain.model.CaptureEventEntity
import com.hanadulset.pro_poseapp.domain.model.ViewPortSize
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.CameraState
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.ViewRate
import kotlinx.coroutines.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.UserUIItem
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.toDomain
import com.hanadulset.pro_poseapp.presentation.feature.camera.model.toUIItem
import javax.inject.Inject

import com.hanadulset.pro_poseapp.presentation.feature.camera.model.PoseUIItem
import androidx.core.net.toUri

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val imageUseCases: ImageUseCases, // 이미지 처리 관련 UseCase 허브
    private val aiUseCases: AiUseCases, // AI 관련 UseCase 허브
    private val userUseCases: UserUseCases, // 사용자 설정 관련 UseCase 허브
    private val galleryUseCases: GalleryUseCases, // 갤러리 관련 UseCase 허브
    private val cameraManager: CameraManager // 카메라 기능 직접 제어 매니저
) : ViewModel() {

    private val _userSetState = MutableStateFlow<UserUIItem?>(null)
    val userSetState = _userSetState.asStateFlow()

    private val _backgroundDataState = MutableStateFlow<Pair<Int, List<Double>>?>(null)
    val backgroundDataState = _backgroundDataState.asStateFlow()

    private val _aspectRatioState = MutableStateFlow(VIEW_RATE_LIST[0])
    val aspectRatioState = _aspectRatioState.asStateFlow()

    private val _previewState = MutableStateFlow(CameraState(CameraState.CAMERA_INIT_NOTHING))

    private val _capturedBitmapState = MutableStateFlow<Uri?>(null)
    private val _poseResultState = MutableStateFlow<MutableList<PoseUIItem>?>(null)
    private val _fixedScreenState = MutableStateFlow<Bitmap?>(null)
    private val _modifiedPointState = MutableStateFlow<Offset?>(null)
    private val _selectedPoseItemIndex = MutableStateFlow(1)

    val pointOffsetState = _modifiedPointState.asStateFlow()
    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()
    val fixedScreenState = _fixedScreenState.asStateFlow()
    val previewState = _previewState.asStateFlow()
    val selectedPoseItemIndex = _selectedPoseItemIndex.asStateFlow()

    private var previewSizeState: ComposeSize? = null
    private val _poseOnRecommend = MutableStateFlow(false)
    private var compJob: Job? = null

    // 카메라 프레임 분석기 설정 (람다 대신 명시적 객체 사용하여 리소스 참조 안정화)
    private val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy -> imageUseCases.processImageFrameUseCase(imageProxy) }

    // 카메라 바인딩
    fun bindCameraToLifeCycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        previewRotation: Int
    ) {
        _previewState.value = CameraState(cameraStateId = CameraState.CAMERA_INIT_ON_PROCESS)
        viewModelScope.launch {
            val res = cameraManager.bindCamera(
                lifecycleOwner,
                surfaceProvider,
                aspectRatio = aspectRatioState.value.aspectRatioType,
                previewRotation = previewRotation,
                analyzer = imageAnalyzer
            )
            _previewState.value = res
        }
    }

    // 사진 촬영
    fun getPhoto() {
        viewModelScope.launch {
            compJob?.cancel() // 사진 촬영 시 기존 구도 추천(트래킹) 해제
//            cameraManager.sendShutterSound()
            val imageProxy = cameraManager.takePhoto()
            
            val poseIndex = _selectedPoseItemIndex.value
            val poseList = _poseResultState.value
            val backgroundData = _backgroundDataState.value
            
            val captureEventData = CaptureEventEntity(
                poseID = if (poseList != null && poseIndex in poseList.indices) poseList[poseIndex].poseId else -1,
                prevRecommendPoses = poseList?.map { it.poseId },
                timestamp = System.currentTimeMillis().toString(),
                backgroundId = backgroundData?.first,
                backgroundHog = backgroundData?.second
            )

            imageProxy.use { proxy ->
                val uriString = imageUseCases.captureImageUseCase(proxy, captureEventData)
                _capturedBitmapState.update { uriString.toUri() }
            }
        }
    }

    fun selectPoseItem(index: Int) {
        _selectedPoseItemIndex.value = index
    }

    fun getViewRateList() = VIEW_RATE_LIST

    // 포즈 추천 요청
    fun reqPoseRecommend() {
        if (_poseOnRecommend.value.not()) {
            _poseOnRecommend.value = true
            _poseResultState.value = null
            viewModelScope.launch {
                val recommendedData = aiUseCases.recommendPoseUseCase()
                // 추천된 데이터를 UI 상태에 직접 반영
                _poseResultState.update {
                    val mappedList = recommendedData.poseDataList.map { data ->
                        PoseUIItem(
                            poseId = data.poseId,
                            poseCat = data.poseCat,
                            bottomCenterRate = SizeF(data.bottomCenterRate.width, data.bottomCenterRate.height),
                            sizeRate = SizeF(data.sizeRate.width, data.sizeRate.height),
                            imageUri = data.imageUri?.toUri() ,
                            imageScale = data.imageScale
                        )
                    }.toMutableList()
                    
                    mappedList.apply {
                        add(0, PoseUIItem(poseId = -1, poseCat = -1)) // 전체보기용 더미 데이터 삽입
                    }
                    
                    _userSetState.value?.let { userSet ->
                        if (mappedList.size > userSet.poseCnt + 1) mappedList.subList(0, userSet.poseCnt + 1).toMutableList()
                        else mappedList
                    } ?: mappedList
                }
                _backgroundDataState.update {
                    Pair(recommendedData.backgroundId, recommendedData.backgroundAngleList)
                }
                _poseOnRecommend.value = false
            }
        }
    }

    // 구도 권장/추적 요청 (실시간 Flow 관찰 시작)
    fun reqCompRecommend(previewSize: ComposeSize) {
        Log.d("CameraViewModel:", "Running ReqCompRecommend")
        previewSizeState = previewSize
        compJob?.cancel()
        compJob = viewModelScope.launch {
            var nullStreakJob: Job? = null
            Log.d("CameraViewModel", "Starting to collect recommendCompInfo flow")
            aiUseCases.recommendCompInfoUseCase().collect { res ->
                Log.d("CameraViewModel", "Received update from AIRepository: $res")
                if (res != null) {
                    nullStreakJob?.cancel()
                    _modifiedPointState.update {
                        previewSizeState?.center?.let { center ->
                            Offset(
                                center.x * (1F + res.first * 2),
                                center.y * (1F + res.second * 2)
                            )
                        }
                    }
                } else {
                    // 추적 손실 시 즉시 지우지 않고 버퍼 시간을 두어 깜빡임 완화
                    if (nullStreakJob?.isActive != true) {
                        nullStreakJob = launch {
                            kotlinx.coroutines.delay(100) // 100ms 대기 (약 3~4프레임)
                            _modifiedPointState.update { null }
                        }
                    }
                }
            }
        }
    }



    // 카메라 포커스 설정
    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        cameraManager.setFocus(meteringPoint, durationMilliSeconds)
    }   

    fun setZoomLevel(zoomLevel: Float) = cameraManager.setZoom(zoomLevel)

    // 화면 비율 변경
    fun changeViewRate(idx: Int): Boolean {
        val res = _aspectRatioState.value.aspectRatioType == VIEW_RATE_LIST[idx].aspectRatioType
        if (res.not()) {
            _aspectRatioState.value = VIEW_RATE_LIST[idx]
            compJob?.cancel()
            _modifiedPointState.update { null }
        }
        return res
    }

    // 고정 화면 제어
    fun controlFixedScreen(isRequest: Boolean) {
        if (isRequest) {
            viewModelScope.launch {
                // 파라미터 없이 호출하여 UseCase 내부에서 실시간 프레임을 획득하도록 함
                val res: Bitmap? = imageUseCases.showFixedScreenUseCase()
                _fixedScreenState.value = res
            }
        } else _fixedScreenState.value = null
    }

    // 이미지로부터 포즈 정보 추출
    fun getPoseFromImage(uri: Uri) {
        viewModelScope.launch {
            _fixedScreenState.value = null
            // 타입 유추를 통한 호출
            val res: Bitmap? = aiUseCases.getPoseFromImageUseCase(uri.toString())
            _fixedScreenState.value = res
            galleryUseCases.deleteImageFromPicturesUseCase(uri.toString())
        }
    }

    // 최근 촬영 이미지 획득 (썸네일용)
    fun getLastImage() {
        viewModelScope.launch {
            val uriString = imageUseCases.getLatestImageUseCase()
            _capturedBitmapState.value = uriString?.toUri()
        }
    }

    // 사용자 설정 로드
    fun loadUserSet() {
        viewModelScope.launch {
            _userSetState.value = userUseCases.loadUserSetUseCase().toUIItem()
        }
    }

    // 사용자 설정 저장
    fun saveUserSet(userSet: UserUIItem) {
        viewModelScope.launch {
            userUseCases.saveUserSetUseCase(userSet.toDomain())
            _userSetState.value = userSet
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.unbind()
    }

    companion object {
        // 화면 비율 리스트 상수화
        private val VIEW_RATE_LIST = listOf(
            ViewRate(
                name = "4:3", aspectRatioType = AspectRatio.RATIO_4_3, aspectRatioSize = Size(3, 4)
            ), ViewRate(
                "16:9", aspectRatioType = AspectRatio.RATIO_16_9, aspectRatioSize = Size(9, 16)
            )
        )
    }
}

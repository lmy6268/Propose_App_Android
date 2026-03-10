package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Range
import android.util.SizeF
import android.view.OrientationEventListener
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.center
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object CameraScreenCompScreen {
    const val SHAKE_THRESHOLD_DISTANCE = 80F * 80F // Threshold for check shaking


    @Composable
    fun CompScreen(
        modifier: Modifier = Modifier,
        previewSize: () -> SizeF,
        pointOffSet: () -> Offset?,
        triggerPoint: (DpSize) -> Unit,
        onPointMatched: (() -> Boolean) -> Unit,
        stopToTracking: () -> Unit = {} //만약 트래커의 Offset이 화면을 벗어나는 경우, 트래킹을 멈춤
    ) {
        val localDensity = LocalDensity.current //현재 밀도
        val horizontalCheckCircleRadius = 60F
        //구도 추천 화면의 크기
        val compSize = remember {
            mutableStateOf<DpSize?>(null)
        }


        //구도추천 활성화 여부
        val isPointOn = remember { mutableStateOf(false) }

        val horizonState = remember {
            mutableStateOf(false)
        }
        val localModifier = modifier.size(
            localDensity.run {
                DpSize(
                    previewSize().width.toDp(),
                    previewSize().height.toDp()
                )
            }
        )
        Box(
            modifier = localModifier
                .onGloballyPositioned { coordinates ->
                    coordinates.size.let {
                        with(localDensity) {
                            compSize.value = DpSize(
                                it.width.toDp(), it.height.toDp()
                            )
                        }
                    }
                }) {
            if (compSize.value != null) {
                // 이제 센서는 화면이 켜져 있는 동안 딱 한 번만 등록됩니다.
                SensorTrigger(
                    enabled = !isPointOn.value // 포인트가 없을 때만 흔들림 감지 로직 작동
                ) {
                    isPointOn.value = true
                    triggerPoint(compSize.value!!)
                }

                // 구도 추천 포인트 표시
                if (isPointOn.value && pointOffSet() != null) {
                    CompGuidePoint(
                        areaSize = { compSize.value!! },
                        pointOffSetState = { pointOffSet()!! },
                        onPointMatched = {
                            onPointMatched { horizonState.value }
                        },
                        isOnHorizon = { horizonState.value },
                        onStopToTracking = {
                            // 포인트 추적 중단 시 다시 센서가 작동할 수 있게 상태 변경
                            isPointOn.value = false
                            stopToTracking()
                        }
                    )
                }

                //수평계
                HorizontalCheckModule(
                    modifier = localModifier,
                    isShowHorizontalCheck = { compSize.value != null },
                    centerRadius = horizontalCheckCircleRadius,
                    centroid = with(localDensity) {
                        compSize.value.let {
                            Offset(
                                (it!!.width / 2).toPx(), (it.height / 2).toPx()
                            )
                        }
                    },
                    onMakeHorizontalEvent = {
                        horizonState.value = it
                    }
                )
            }
        }


    }

    private fun checkPointInBoundary(
        offset: () -> Offset,
        areaSize: () -> DpSize,
        localDensity: Density,
        onStopToTracking: () -> Unit
    ) {
        offset().run {
            val comp = with(localDensity) {
                areaSize().let {
                    Size(
                        it.width.toPx(), it.height.toPx()
                    )
                }
            }
            val xRange = Range(0F, comp.width)
            val yRange = Range(0F, comp.height)
            val isInBoundary = this.x in xRange && this.y in yRange //영역 내에 포인트가 있는지 확인
            if (isInBoundary.not()) {
                onStopToTracking() //구도 포인트를 제거함
            }
        }
    }

    @Composable
    private fun CompGuidePoint(
        modifier: Modifier = Modifier,
        areaSize: () -> DpSize,
        pointOffSetState: () -> Offset?,
        pointColor: Color = Color(0x80FFFFFF),
        pointRadius: Float = 55F,
        onPointMatched: () -> Unit,
        isOnHorizon: () -> Boolean,
        onStopToTracking: () -> Unit
    ) {
        val localDensity = LocalDensity.current
        val areaCentroid = LocalDensity.current.run {
            with(areaSize().center) { Offset(x.toPx(), y.toPx()) }
        } //화면 중심부의 좌표

        val isMatched = remember {
            mutableStateOf(false)
        }
        val localColor = LocalColors.current
        val isTriggered = remember {
            mutableStateOf(false)
        }
        val onHorizon by rememberUpdatedState(newValue = isOnHorizon)

        if (pointOffSetState() != null) {
            //현재 구도 포인트의 위치
            val pointOffset = rememberUpdatedState {
                //거리계산
                val distance = with(Pair(areaCentroid, pointOffSetState()!!)) {
                    sqrt((first.x - second.x).pow(2) + (first.y - second.y).pow(2))
                }
                val catchThreshold = 50F
                if (distance in 0F..catchThreshold) {
                    //색 변경
                    isMatched.value = true
                    //위치 고정
                    areaCentroid
                } else {
                    //원래대로 색 되돌리기
                    isMatched.value = false
                    pointOffSetState()!!
                }
            }
            LaunchedEffect(pointOffset.value()) {
                checkPointInBoundary(
                    offset = { pointOffset.value() },
                    areaSize = areaSize,
                    localDensity = localDensity,
                    onStopToTracking = onStopToTracking,
                )
            }

            LaunchedEffect(isMatched.value, onHorizon)
            {
                if (isMatched.value && isTriggered.value.not() && onHorizon()) {
                    onPointMatched()
                    isTriggered.value = true
                }
            }

            Box(
                modifier = modifier.size(areaSize())
            )
            {
                Canvas(
                    modifier = Modifier
                ) {
                    if (isMatched.value.not()) drawCircle(
                        center = pointOffset.value(),
                        radius = pointRadius,
                        color = pointColor,
                    )
                    else {
                        drawCircle(
                            center = pointOffset.value(),
                            radius = pointRadius,
                            color = Color(0x90FFFF00),
                            style = Stroke(
                                width = 10F
                            )
                        )
                        drawCircle(
                            center = pointOffset.value(),
                            radius = pointRadius,
                            color = localColor.primaryBlue100.copy(alpha = 0.8f),
                        )
                    }
                }
            }

        }


    }

    @Composable
    fun SensorTrigger(
        enabled: Boolean, // 활성화 여부 추가
        onTracking: () -> Unit,
    ) {
        val context = LocalContext.current
        // 1. 센서 관련 객체들을 remember로 감싸서 재구성(Recomposition) 시 재생성 방지
        val sensorManager =
            remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
        val gyroscopeSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

        val comboNonShakingDTThreshold = 0.4F // 유지 시간 임계값

        // 2. timestamp를 Long으로 변경하여 정밀도 유지 및 FloatState로 관리
        val timestamp = remember { mutableLongStateOf(0L) }
        val shakeState = remember { mutableStateOf(true) }

        // 3. comboDT를 remember로 관리하여 리렌더링 시에도 값이 초기화되지 않게 함
        val comboDT = remember { mutableFloatStateOf(0f) }

        val sensorListener = remember {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (!enabled) return // [추가] enabled가 false면 계산하지 않고 리
                    if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                        val (x, y, z) = event.values
                        val currentEventTime = event.timestamp

                        if (timestamp.longValue != 0L) {
                            // 나노초(ns)를 초(s) 단위로 변환
                            val dt =
                                (currentEventTime - timestamp.longValue) * (1.0F / 1000000000.0F)

                            // dt가 너무 작거나 0인 경우 계산 건너빰
                            if (dt > 0) {
                                val dx = abs(x * dt * 1000)
                                val dy = abs(y * dt * 1000)
                                val dz = abs(z * dt * 1000)
                                val magnitude = dx * dx + dy * dy + dz * dz

                                if (magnitude < SHAKE_THRESHOLD_DISTANCE) {
                                    // 흔들림이 멈춘 상태가 임계값(0.4초) 이상 지속되었는지 확인
                                    if (shakeState.value && comboDT.floatValue >= comboNonShakingDTThreshold) {
                                        shakeState.value = false // 흔들림 멈춤 상태로 변경
                                        comboDT.floatValue = 0f
                                    } else {
                                        comboDT.floatValue += dt
                                    }
                                } else {
                                    // 다시 흔들림이 감지되면 상태 초기화
                                    if (!shakeState.value) shakeState.value = true
                                    comboDT.floatValue = 0f
                                }
                            }
                        }
                        timestamp.longValue = currentEventTime
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }
        }

        // 4. [핵심] 리스너 등록 및 해제는 컴포저블의 생명주기에만 맞춤 (Unit 사용)
        // shakeState.value가 바뀐다고 해서 register/unregister를 반복하지 않음
        DisposableEffect(Unit) {
            sensorManager.registerListener(
                sensorListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            onDispose {
                sensorManager.unregisterListener(sensorListener)
            }
        }

        // 5. shakeState가 false가 되는 순간(흔들림 멈춤)에만 콜백 실행
        LaunchedEffect(shakeState.value) {
            if (!shakeState.value) {
                onTracking()
            }
        }
    }


    // 수평계 모듈
    @Composable
    fun HorizontalCheckModule(
        isShowHorizontalCheck: () -> Boolean = { false },
        modifier: Modifier = Modifier,
        centerRadius: Float,
        centroid: Offset,
        onMakeHorizontalEvent: (Boolean) -> Unit = {}
    ) {
        val context = LocalContext.current

        val shortLineLength = 30F

        val rotationState = remember {
            mutableIntStateOf(0)
        }
        val angleThreshold = 5

        val rotationEventListener = remember {
            object : OrientationEventListener(context.applicationContext) {
                override fun onOrientationChanged(orientation: Int) {
                    // -1이 나오면 측정을 중지한다.
                    if (orientation != -1) {
                        rotationState.intValue = when (orientation) {
                            in 180 - angleThreshold..180 + angleThreshold -> {
                                onMakeHorizontalEvent(true)
                                180
                            }

                            in 0..angleThreshold -> {
                                onMakeHorizontalEvent(true)
                                0
                            }

                            in 360 - angleThreshold..360 -> {
                                onMakeHorizontalEvent(true)
                                360
                            }

                            else -> {
                                onMakeHorizontalEvent(false)
                                orientation
                            }
                        }

                    }

                }
            }
        }


        DisposableEffect(isShowHorizontalCheck()) {
            rotationEventListener.enable() //시작
            onDispose {
                rotationEventListener.disable()//종료
            }
        }

        Canvas(
            modifier = modifier,
        ) {
            val leftEndOffset = Offset((centroid.x - centerRadius), centroid.y)
            val leftStartOffset = Offset(leftEndOffset.x - shortLineLength, centroid.y)
            val rightStartOffset = Offset((centroid.x + centerRadius), centroid.y)
            val rightEndOffset = Offset(rightStartOffset.x + shortLineLength, centroid.y)

            //회전을 감지 한다.
            rotate(
                -rotationState.intValue.toFloat(), pivot = centroid //회전 기준점
            ) {
                val calibrationDegree = -rotationState.intValue
                if (calibrationDegree in listOf(0, -180)) {
                    drawLine(
                        start = leftStartOffset,
                        end = rightEndOffset,
                        strokeWidth = 10F,
                        color = Color(0x90FFFF00)
                    )
                    drawCircle(
                        color = Color(0x90FFFF00),
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 2F
                        )
                    )
                } else {
                    drawLine(
                        Color.White, leftStartOffset, leftEndOffset, strokeWidth = 10F
                    )
                    drawLine(
                        Color.White, rightStartOffset, rightEndOffset, strokeWidth = 10F
                    )
                    drawCircle(
                        color = Color(0x90FFFFFF),
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 3F
                        )
                    )
                }


            }
        }
    }


}

@Preview
@Composable
fun PreviewHorizontal() {
    CameraScreenCompScreen.HorizontalCheckModule(
        modifier = Modifier.fillMaxSize(), centerRadius = 30F, centroid = Offset(500F, 500F)
    )
}
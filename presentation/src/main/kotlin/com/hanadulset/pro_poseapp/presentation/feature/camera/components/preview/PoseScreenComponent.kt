package com.hanadulset.pro_poseapp.presentation.feature.camera.components.preview

import android.util.LayoutDirection
import android.util.SizeF
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Scale
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.pose.model.PoseUIItem

object PoseScreenComponent {
    @Composable
    fun ShowingPoseScreen(
        modifier: Modifier = Modifier,
        previewViewSize: () -> DpSize,
        poseData: PoseUIItem,
        poseScale: () -> Float,
        poseOffset: () -> SizeF?,
        onLimitMaxScale: (Float) -> Unit,
        onChangeOffset: (SizeF) -> Unit //오프셋을 이동시키면, 이 메소드가 실행됨
    ) {
        val localDensity = LocalDensity.current
        //현재 포즈 아이템
        val currentItem by rememberUpdatedState(newValue = poseData)
        val scaleOfPose by rememberUpdatedState(newValue = poseScale())
        val offsetOfPose by rememberUpdatedState(newValue = poseOffset())
        val boxSize by rememberUpdatedState(newValue = localDensity.run {
            previewViewSize().let {
                SizeF(it.width.toPx(), it.height.toPx())
            }
        })


        //미리보기를 채우는 박스
        Box(
            modifier = modifier
        ) {
            AnimatedContent(
                targetState = currentItem,
                transitionSpec = {
                    ContentTransform(
                        initialContentExit = fadeOut(),
                        targetContentEnter = fadeIn()
                    )
                },
                label = "포즈가 보이는 화면",
            ) { poseItem ->
                //이미지 기본크기
                val poseItemSize = remember {
                    localDensity.run {
                        mutableStateOf(
                            Size(
                                (boxSize.width * poseItem.sizeRate.width),
                                (boxSize.height * poseItem.sizeRate.height)
                            )
                        )
                    }
                }
                //아직 어색함
                val poseBottomRightOffset = remember {
                    localDensity.run {
                        mutableStateOf(
                            if (offsetOfPose != null) Offset(
                                offsetOfPose!!.width + poseItemSize.value.width,
                                offsetOfPose!!.height + poseItemSize.value.height
                            )
                            else Offset(
                                boxSize.width * poseItem.bottomCenterRate.width + poseItemSize.value.width / 2,
                                boxSize.height * poseItem.bottomCenterRate.height + poseItemSize.value.height / 2
                            )
                        )
                    }
                }
                //포즈 이미지 페인터
                val painter = poseItem.imageUri?.let { uri ->
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current).data(uri).size(
                            coil.size.Size(
                                Dimension(
                                    poseItemSize.value.width.toInt()
                                ), Dimension(
                                    poseItemSize.value.height.toInt()
                                )
                            )
                        ).scale(Scale.FIT).build()
                    )
                }


                val isChecked = remember {
                    mutableStateOf(false)
                }
                val poseItemInitScale = remember { mutableStateOf<Float?>(null) }


                LaunchedEffect(key1 = painter?.state) {
                    if (isChecked.value.not()) {
                        localDensity.run {
                            (painter?.state as? AsyncImagePainter.State.Success)?.painter?.intrinsicSize?.let { originImageSizeByDp ->
                                val originImageSize = Size(
                                    originImageSizeByDp.width.dp.toPx(),
                                    originImageSizeByDp.height.dp.toPx(),
                                )
                                val scale =
                                    if (originImageSize.width > originImageSize.height) poseItemSize.value.width / originImageSize.width
                                    else poseItemSize.value.height / originImageSize.height
                                poseItemSize.value = originImageSize.run {
                                    Size(
                                        width = width * scale, height = height * scale
                                    )
                                }
                                poseItemInitScale.value = scale
                                isChecked.value = true
                            }
                        }
                    }
                }


                //포즈 아이템에 대한 설정을 진행한다.
                // 필요한 설정 : 기본 이미지 사이즈, offset 이동 처리,
                if (poseItemInitScale.value != null) {
                    val poseTopLeftOffset = remember {
                        localDensity.run {
                            mutableStateOf(
                                if (offsetOfPose != null) Offset(
                                    offsetOfPose!!.width,
                                    offsetOfPose!!.height
                                )
                                else {
                                    Offset(
                                        boxSize.width * poseItem.bottomCenterRate.width - (poseItemSize.value.width / 2),
                                        boxSize.height * poseItem.bottomCenterRate.height - poseItemSize.value.height
                                    )
                                }
                            )
                        }
                    }

                    val calculateMaxScale: () -> Float = {
                        val nowPoseTopLeftOffset = poseTopLeftOffset.value
                        val originPoseSize = poseItemSize.value
                        val maxSize = SizeF(
                            boxSize.width - nowPoseTopLeftOffset.x,
                            boxSize.height - nowPoseTopLeftOffset.y
                        )
                        val resultValue = floatArrayOf(
                            maxSize.width / originPoseSize.width,
                            maxSize.height / originPoseSize.height
                        ).min()
                        resultValue
                    }
                    LaunchedEffect(key1 = Unit) {
                        poseTopLeftOffset.value = poseTopLeftOffset.value.run {
                            Offset(
                                x.coerceIn(0f, boxSize.width), y.coerceIn(0f, boxSize.height)
                            )
                        }
                    }
                    val currentPoseSize by rememberUpdatedState(newValue = localDensity.run {
                        DpSize(poseItemSize.value.width.toDp(), poseItemSize.value.height.toDp())
                    })

                    PoseItem(modifier = Modifier

                        .size(
                            currentPoseSize
                        )
                        .onSizeChanged {
                            poseItemSize.value = Size(
                                it.width.toFloat(), it.height.toFloat()
                            )
                        }
                        .offset {
                            onLimitMaxScale(calculateMaxScale())
                            poseTopLeftOffset.value.run {
                                IntOffset(x.toInt(), y.toInt())
                            }
                        }
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0f)
                            localDensity.run {
                                //Top_left 기준 픽셀 오프셋 -> 이걸 가지고 중심점 값과 잘 대비 및 분류해두자.
                                //스케일 관련
                                transformOrigin =
                                    TransformOrigin(0f, 0f) //top-left 기준으로 사이즈를 늘려나가자.
                                scaleX = scaleOfPose
                                scaleY = scaleOfPose
                            }
                        }
                        .pointerInput(Unit) {
                            //드래그를 인식하고 반영한다. -> dragAmount 만큼 이동
                            detectDragGestures { change, dragAmount ->
                                val checkOffset = dragAmount * scaleOfPose + poseTopLeftOffset.value
                                poseTopLeftOffset.value = checkOffset.run {
                                    Offset(
                                        x.coerceIn(
                                            0f,
                                            boxSize.width - (poseItemSize.value.width * scaleOfPose)
                                        ), y.coerceIn(
                                            0f,
                                            boxSize.height - (poseItemSize.value.height * scaleOfPose)
                                        )
                                    )
                                }
                                onChangeOffset(poseTopLeftOffset.value.run { SizeF(x, y) })
                            }
                        }
//                        .mirror()
                        , painter = painter
                    )
                }
            }
        }
    }


    @Composable
    fun PoseItem(
        modifier: Modifier = Modifier, painter: Painter? = null
    ) {
        painter.run {
            if (this == null) Box(modifier = modifier)
            else Image(
                painter = this,
                modifier = modifier,
                contentScale = ContentScale.Fit,
                contentDescription = ""
            )
        }
    }

    @Stable
    fun Modifier.mirror(): Modifier = composed {
        if (LocalLayoutDirection.current.ordinal == LayoutDirection.LTR)
            this.scale(scaleX = -1f, scaleY = 1f)
        else
            this
    }
}

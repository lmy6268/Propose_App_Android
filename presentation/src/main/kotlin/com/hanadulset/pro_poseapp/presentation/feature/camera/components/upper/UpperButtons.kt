package com.hanadulset.pro_poseapp.presentation.feature.camera.components.upper

import android.os.SystemClock
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.presentation.R
object UpperButtons {
    //확장가능한 버튼
    @Composable
    fun ExpandableButton(
        itemList: List<String>, // 내부에 들어갈 값
        type: String,  // 속성 값
        modifier: Modifier = Modifier,
        onSelectedItemEvent: (Int) -> Unit,
        isExpanded: (Boolean) -> Unit,
        defaultButtonSize: Dp = 44.dp,
        defaultButtonColor: Color = LocalColors.current.textPrimary100,
        triggerClose: () -> Boolean,
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val selectedIndexState = rememberSaveable { mutableIntStateOf(0) }
        val closeExpandedWindow by rememberUpdatedState(newValue = {
            isExpandedState.value = false
            isExpanded(false)
        })

        val onBtnClicked by rememberUpdatedState(newValue = {
            isExpandedState.value = true
            isExpanded(true)
        })

        val expandableBtnSize = remember {
            mutableStateOf(DpSize(100.dp, 100.dp))
        }

        if (isExpandedState.value) {
            LaunchedEffect(key1 = triggerClose()) {
                if (triggerClose()) closeExpandedWindow()
            }
        }
        Box(
            modifier = modifier
                .animateContentSize(
                    //크기 변경이 감지되면 애니메이션을 추가해준다.
                    animationSpec = tween(
                        durationMillis = 200, easing = LinearEasing
                    )
                )
                .onGloballyPositioned { coordinates ->
                    coordinates.size.let {
                        expandableBtnSize.value = DpSize(it.width.dp, it.height.dp)
                    }
                }

        ) {
            if (isExpandedState.value) Box(
                modifier
                    .height(defaultButtonSize)
                    .fillMaxWidth()
                    .background(
                        color = defaultButtonColor.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(30.dp)
                    )
            ) {
                //닫는 버튼
                IconButton(
                    modifier = Modifier
                        .size(defaultButtonSize)
                        .align(Alignment.CenterStart),
                    onClick = {
                        closeExpandedWindow()
                    }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        modifier = Modifier.border(
                            width = 3.dp,
                            shape = CircleShape,
                            color = defaultButtonColor.copy(alpha = 0.8f)
                        ),
                        tint = Color.White,
                        contentDescription = "background",
                    )
                    Icon(
                        painterResource(id = R.drawable.close),
                        contentDescription = "close",
                    )
                }
                //선택지 화면
                Row(
                    Modifier
                        .wrapContentHeight()
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (idx in itemList.indices) {
                        Box(
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    selectedIndexState.intValue = idx
                                    onSelectedItemEvent(idx)
                                }
                                .wrapContentSize()
                                .padding(10.dp)) {
                            Text(
                                text = itemList[idx],
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (selectedIndexState.intValue == idx) FontWeight.Bold else FontWeight.Light,
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }
            }
            else IconButton(
                modifier = modifier, onClick = onBtnClicked
            ) {
                Icon(
                    modifier = Modifier
                        .size(defaultButtonSize)
                        .border(
                            BorderStroke(2.dp, LocalColors.current.textPrimary100),
                            shape = CircleShape
                        ),
                    painter = painterResource(id = R.drawable.based_circle),
                    tint = LocalColors.current.background100,
                    contentDescription = type
                )
                Text(
                    textAlign = TextAlign.Center,
                    color = LocalColors.current.textPrimary100,
                    text = itemList[selectedIndexState.intValue], //화면 비 글씨 표기
                    fontWeight = FontWeight(FontWeight.Bold.weight),
                    fontSize = 14.sp
                )
            }

        }
    }

}

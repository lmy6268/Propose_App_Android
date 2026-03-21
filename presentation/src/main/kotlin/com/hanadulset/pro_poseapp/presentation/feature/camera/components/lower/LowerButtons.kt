package com.hanadulset.pro_poseapp.presentation.feature.camera.components.lower

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
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.CommonButtons

object LowerButtons {
    private object LocalButtonRippleTheme : RippleTheme {

        private var defaultColor = Color.White
        private var alphaColor = Color.Black
        fun setRippleEffect(
            defaultColor: Color = Color.Unspecified,
            alphaColor: Color = Color.Black
        ) {
            this.defaultColor = defaultColor
            this.alphaColor = alphaColor
        }

        @Composable
        override fun defaultColor() = RippleTheme.defaultRippleColor(
            defaultColor, lightTheme = true
        )

        @Composable
        override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
            alphaColor, lightTheme = true
        )
    }

    @Composable
    fun ToggledButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 30.dp,
        buttonStatus: Boolean,
        activatedColor: Color = LocalColors.current.primaryBlue100,
        inActivatedColor: Color = LocalColors.current.textSecondary80,
        buttonDescription: String = "버튼",
        buttonText: String = "",
        buttonTextColor: Color = Color.White,
        iconDrawableId: Int = R.drawable.based_circle,
        innerIconDrawableId: Int? = null,
        onClickEvent: () -> Unit
    ) {
        val beforeTime = remember(onClickEvent) {
            mutableLongStateOf(0L)
        }
        val INTERVAL = 500L
        val buttonState by rememberUpdatedState(newValue = buttonStatus)

        CompositionLocalProvider(LocalRippleTheme provides LocalButtonRippleTheme.apply {
            setRippleEffect(
                alphaColor = if (buttonState) activatedColor else inActivatedColor,
                defaultColor = Color.Black
            )
        }) {
            IconButton(
                modifier = modifier.size(buttonSize),
                onClick = {
                    val clickedTime = SystemClock.elapsedRealtime()
                    if ((clickedTime - beforeTime.longValue) >= INTERVAL) {
                        onClickEvent()
                        beforeTime.longValue = clickedTime
                    }
                }
            ) {
                Icon(
                    modifier = modifier.size(buttonSize),
                    painter = painterResource(id = iconDrawableId),
                    contentDescription = buttonDescription,
                    tint = if (buttonState) activatedColor else inActivatedColor
                )
                if (buttonText != "") Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    color = if (buttonState.not()) buttonTextColor else Color.Black,
                    fontFamily = CommonButtons.pretendardFamily,
                    fontWeight = FontWeight.Bold
                )
                else if (innerIconDrawableId != null) Icon(
                    painter = painterResource(id = innerIconDrawableId),
                    tint = if (buttonState.not()) buttonTextColor else Color.Black,
                    modifier = Modifier.size(buttonSize / 2),
                    contentDescription = "내부 아이콘"
                )

            }
        }
    }

    @Composable
    fun ParticularZoomButton(
        selected: Boolean,
        defaultButtonSize: Dp,
        buttonValue: Int,
        selectedButtonScale: Float = 2F,
        selectedButtonColor: Color = LocalColors.current.textPrimary100,
        unSelectedButtonColor: Color = LocalColors.current.background100,
        onClickEvent: () -> Unit
    ) {
        val mutableInteractionSource = remember { MutableInteractionSource() }

        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = mutableInteractionSource,
                    indication = rememberRipple(
                        color = Color(0xFF999999), bounded = true, radius = defaultButtonSize / 2
                    ), //Ripple 효과 제거,
                    onClick = onClickEvent
                )
                .size(defaultButtonSize)
                .scale(
                    if (selected) selectedButtonScale else 1F
                )

        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.based_circle),
                contentDescription = "줌버튼",
                tint = if (selected) selectedButtonColor else unSelectedButtonColor
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = if (selected) "${buttonValue}X" else buttonValue.toString(),
                fontSize = 10.sp,
                color = if (selected) LocalColors.current.primaryBlue100 else LocalColors.current.textPrimary100,
                fontFamily = CommonButtons.pretendardFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Light

            )
        }
    }

    @Composable
    fun FixedButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 80.dp,
        onFixedButtonPressedEvent: () -> Unit,
        fixedBtnStatus: Boolean,
    ) {
        val isFixedBtnPressed by rememberUpdatedState(newValue = fixedBtnStatus)
//        val fixedVector =
        val fixedBtnImage = if (isFixedBtnPressed) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed

        val activatedColor = LocalColors.current.primaryBlue100
        val inActivatedColor = LocalColors.current.textSecondary100


        val mutableInteractionSource = remember { MutableInteractionSource() }

        Surface(
            modifier = modifier
                .wrapContentSize()
                .clickable(
                    indication = rememberRipple(
                        color = if (isFixedBtnPressed) activatedColor.compositeOver(
                            Color.Black
                        )
                        else inActivatedColor.compositeOver(Color.Black),
                        bounded = true,
                        radius = buttonSize / 2
                    ), //Ripple 효과 제거
                    interactionSource = mutableInteractionSource,
                    onClick = {
                        onFixedButtonPressedEvent()
                    }
                ), shape = CircleShape
        ) {
            Icon(
                modifier = Modifier.size(buttonSize),
                painter = painterResource(fixedBtnImage),
                tint = Color.Unspecified,
                contentDescription = "고정버튼"
            )
        }


    }

    @Composable
    fun ShutterButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 80.dp,
        onClickEvent: () -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        //버튼 이미지 배치
        val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
        else R.drawable.ic_shutter_normal
        val imageDrawable = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current).data(buttonImg)
                .size(LocalDensity.current.run {
                    buttonSize.toPx().toInt()
                }) //뷰 사이즈의 크기 만큼 이미지 리사이징
                .build()
        )

        Surface(
            modifier = modifier
                .clickable(
                    indication = rememberRipple(
                        color = LocalColors.current.textSecondary100,
                        bounded = true,
                        radius = buttonSize / 2
                    ), //Ripple 효과 제거
                    interactionSource = interactionSource,
                    onClick = onClickEvent
                ), shape = CircleShape
        ) {
            Icon(
                modifier = modifier.size(buttonSize),
                tint = Color.Unspecified,
                painter = imageDrawable,
                contentDescription = "촬영버튼"
            )
        }
    }


}

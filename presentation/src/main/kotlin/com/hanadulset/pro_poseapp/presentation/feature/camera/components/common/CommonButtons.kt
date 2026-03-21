package com.hanadulset.pro_poseapp.presentation.feature.camera.components.common

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
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.CommonButtons.SwitchableButton

object CommonButtons {
    val pretendardFamily = FontFamily(
        Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
    )

    @Composable
    fun SwitchableButton(
        modifier: Modifier = Modifier,
        init: Boolean,
        buttonSize: DpSize = DpSize(23.dp, 15.dp),
        positiveColor: Color,
        negativeColor: Color,
        onChangeState: (Boolean) -> Unit,
        isEnabled: () -> Boolean = { true },
        scale: Float = 2f,
        strokeWidth: Dp = (buttonSize.height / 10),
        gapBetweenThumbAndTrackEdge: Dp = (buttonSize.width / 9),
    ) {

        val switchON = rememberSaveable { mutableStateOf(init) }
        val thumbRadius = (buttonSize.height / 2) - gapBetweenThumbAndTrackEdge
        // To move thumb, we need to calculate the position (along x axis)
        val animatePosition =
            animateFloatAsState(
                targetValue = if (switchON.value) with(LocalDensity.current) { (buttonSize.width - thumbRadius - gapBetweenThumbAndTrackEdge).toPx() }
                else with(LocalDensity.current) { (thumbRadius + gapBetweenThumbAndTrackEdge).toPx() },
                label = ""
            )
        Column(modifier) {
            Canvas(
                modifier = Modifier
                    .size(buttonSize)
                    .scale(scale = scale)
                    .then(
                        if (isEnabled()) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    // This is called when the user taps on the canvas
                                    switchON.value = !switchON.value
                                    onChangeState(switchON.value)
                                })
                            }
                        } else Modifier
                    )
            ) {
                // Track
                drawRoundRect(
                    color = if (switchON.value) positiveColor else negativeColor,
                    cornerRadius = CornerRadius(
                        x = buttonSize.height.toPx(),
                        y = buttonSize.height.toPx()
                    ),
                    style = Stroke(width = strokeWidth.toPx())
                )

                // Thumb
                drawCircle(
                    color = if (switchON.value) positiveColor else negativeColor,
                    radius = thumbRadius.toPx(),
                    center = Offset(
                        x = animatePosition.value, y = size.height / 2
                    )
                )
            }

        }


    }

    object CustomIndication : Indication {
        private class DefaultDebugIndicationInstance(
            private val isPressed: State<Boolean>,
        ) : IndicationInstance {
            override fun ContentDrawScope.drawIndication() {
                drawContent()
                if (isPressed.value) {
                    drawCircle(color = Color.Gray.copy(alpha = 0.3f))
                }
            }
        }

        @Composable
        override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
            val isPressed = interactionSource.collectIsPressedAsState()
            return remember(interactionSource) {
                DefaultDebugIndicationInstance(isPressed)
            }
        }
    }
    @Composable
    fun NormalButton(
        modifier: Modifier = Modifier,
        isButtonEnable: Boolean = true,
        buttonSize: Dp = 20.dp,
        buttonName: String,
        innerIconDrawableId: Int? = null,
        innerIconDrawableSize: Dp = 10.dp,
        innerIconColorTint: Color = Color.White,
        buttonText: String? = null,
        buttonTextSize: Int = 10,
        buttonTextColor: Color = Color.White,
        colorTint: Color = Color(0x80FAFAFA),
        onClick: () -> Unit
    ) {
        val iconButtonPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.based_circle)
                .build()
        )
        val innerIconDrawablePainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(innerIconDrawableId)
                .build()
        )

        IconButton(
            enabled = isButtonEnable,
            modifier = modifier.size(buttonSize),
            onClick = { onClick() },
        ) {
            Icon(
                modifier = modifier.size(buttonSize),
                painter = iconButtonPainter,
                tint = colorTint,
                contentDescription = buttonName
            )
            if (buttonText != null) Text(
                text = buttonText,
                fontSize = buttonTextSize.sp,
                color = buttonTextColor,
                fontFamily = pretendardFamily,
                fontWeight = FontWeight.Bold
            )
            if (innerIconDrawableId != null) {
                Icon(
                    modifier = Modifier.size(innerIconDrawableSize),
                    painter = innerIconDrawablePainter,
                    contentDescription = "$buttonName 아이콘",
                    tint = innerIconColorTint
                )
            }
        }
    }

}

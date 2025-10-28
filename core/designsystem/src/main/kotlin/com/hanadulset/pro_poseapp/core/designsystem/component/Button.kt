package com.hanadulset.pro_poseapp.core.designsystem.component

import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun ProPoseRippleButton(
    isEnabled: Boolean,
    enabledColor: Color,
    disabledColor: Color,
    isLightTheme: Boolean = true,
    onClick: () -> Unit
) {
    val rippleTheme = remember(isEnabled) {
        object : RippleTheme {
            val color = if (isEnabled) enabledColor else disabledColor
            @Composable
            override fun defaultColor() =
                RippleTheme.defaultRippleColor(color, lightTheme = isLightTheme)

            @Composable
            override fun rippleAlpha() =
                RippleTheme.defaultRippleAlpha(
                    color,
                    lightTheme = isLightTheme
                )
        }
    }
}
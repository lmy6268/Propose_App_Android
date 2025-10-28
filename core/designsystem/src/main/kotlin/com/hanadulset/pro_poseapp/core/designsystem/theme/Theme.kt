package com.hanadulset.pro_poseapp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

// ===== Theme Composable =====
@Composable
fun ProPoseTheme(
    lightColors: ProPoseColors = LightProPoseColors,
    darkColors: ProPoseColors = DarkProPoseColors,
    typography: ProPoseTypography = ProPoseTypography,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColors = if (useDarkTheme) darkColors else lightColors
    val rememberedColors = remember { targetColors }

    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalTypography provides typography
    ) {
        ProvideTextStyle(value = typography.heading01, content = content)
    }
}

// ===== LocalProviders =====
val LocalColors = staticCompositionLocalOf { LightProPoseColors }
val LocalTypography = staticCompositionLocalOf { ProPoseTypography }

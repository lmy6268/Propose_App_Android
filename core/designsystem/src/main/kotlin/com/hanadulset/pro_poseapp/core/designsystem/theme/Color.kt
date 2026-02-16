package com.hanadulset.pro_poseapp.core.designsystem.theme

import androidx.compose.ui.graphics.Color


/**
 * Pro_Pose colors.
 */

//White (Neutral)
internal val White10 = Color(0xFF1B1B1E)
internal val White20 = Color(0xFF303033)
internal val White30 = Color(0xFF464749)
internal val White50 = Color(0xFF77777A)
internal val White60 = Color(0xFF919093)
internal val White80 = Color(0xFFC7C6C9)
internal val White90 = Color(0xFFE3E2E5)
internal val White95 = Color(0xFFF2F0F3)
internal val White98 = Color(0xFFFAF9FC)
internal val White100 = Color(0xFFFFFFFF)

//Blue (Primary)
internal val Blue10 = Color(0xFF001C3B)
internal val Blue30 = Color(0xFF17477E)
internal val Blue40 = Color(0xFF355F98)
internal val Blue80 = Color(0xFFA6C8FF)
internal val Blue90 = Color(0xFFD5E3FF)

//DarkNavy (Secondary)
internal val DarkNavy20 = Color(0xFF29313E)
internal val DarkNavy30 = Color(0xFF3F4755)
internal val DarkNavy40 = Color(0xFF575F6D)
internal val DarkNavy80 = Color(0xFFBFC7D8)
internal val DarkNavy90 = Color(0xFFDBE3F4)

//Red
internal val Red10 = Color(0xFF410002)
internal val Red20 = Color(0xFF690005)
internal val Red30 = Color(0xFF93000A)
internal val Red40 = Color(0xFFBA1A1A)
internal val Red80 = Color(0xFFFFB4AB)
internal val Red90 = Color(0xFFFFDAD6)


class ProPoseColors(
    val primaryBlue100: Color,
    val primaryBlue80: Color,
    val primaryBlue50: Color,
    val background100: Color,
    val background80: Color,
    val textPrimary100: Color,
    val textPrimary80: Color,
    val textSecondary100: Color,
    val textSecondary80: Color,
    val errorRed100: Color,
    val errorRed80: Color,
    val isLight: Boolean
)

val LightProPoseColors = ProPoseColors(
    primaryBlue100 = Blue80,
    primaryBlue80 = Blue90,
    primaryBlue50 = Blue40,
    background100 = White100,
    background80 = White95,
    textPrimary100 = DarkNavy40,
    textPrimary80 = DarkNavy30,
    textSecondary100 = White60,
    textSecondary80 = White50,
    errorRed100 = Red40,
    errorRed80 = Red80,
    isLight = true
)

val DarkProPoseColors = ProPoseColors(
    primaryBlue100 = Blue40,
    primaryBlue80 = Blue30,
    primaryBlue50 = Blue10,
    background100 = White10,
    background80 = White20,
    textPrimary100 = White90,
    textPrimary80 = White80,
    textSecondary100 = White60,
    textSecondary80 = White50,
    errorRed100 = Red80,
    errorRed80 = Red40,
    isLight = false
)


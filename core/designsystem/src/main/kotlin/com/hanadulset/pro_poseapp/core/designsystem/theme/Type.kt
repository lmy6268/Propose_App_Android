package com.hanadulset.pro_poseapp.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.core.designsystem.R

private val PretendardFamily = FontFamily(
    Font(R.font.pretendard_black, FontWeight.Black),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
    Font(R.font.pretendard_extralight, FontWeight.ExtraLight),
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_normal, FontWeight.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_thin, FontWeight.Thin),
)

data object ProPoseTypography {
    val heading01 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 48.sp,
        fontSize = 24.sp
    )

    val heading02 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 32.sp,
        fontSize = 16.sp
    )

    val heading03 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )

    val sub01 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        fontSize = 16.sp
    )

    val sub02 = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
}
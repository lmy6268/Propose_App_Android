package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.core.designsystem.icon.ProPoseIcon
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalColors
import com.hanadulset.pro_poseapp.core.designsystem.theme.LocalTypography
import com.hanadulset.pro_poseapp.core.designsystem.theme.primaryGreen100
import com.hanadulset.pro_poseapp.core.designsystem.theme.subPrimaryBlack100
import com.hanadulset.pro_poseapp.core.designsystem.theme.subSecondaryGray100
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.SwitchableButton

@Composable
fun SettingBoxItemWithToggle(
    modifier: Modifier = Modifier,
    innerText: String,
    innerTextSize: Dp = 23.dp,
    isToggled: () -> Boolean,
    isEnabled: () -> Boolean = { true },
    onToggleEvent: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .background(
                shape = RoundedCornerShape(50.dp),
                color = LocalColors.current.subSecondaryGray100.copy(alpha = 0.2F)
            )
            .padding(horizontal = 20.dp)
            .padding(vertical = 10.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = innerText,
            style = LocalTypography.current.sub01,
            fontSize = LocalDensity.current.run {
                (innerTextSize * 0.7F).toSp()
            },
            color = if (isEnabled()) LocalColors.current.subPrimaryBlack100
            else LocalColors.current.subSecondaryGray100
        )
        SwitchableButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            init = isToggled(),
            negativeColor = if (isEnabled()) (LocalColors.current.subPrimaryBlack100) else LocalColors.current.subSecondaryGray100,
            positiveColor = if (isEnabled()) (LocalColors.current.primaryGreen100) else LocalColors.current.subSecondaryGray100,
            onChangeState = onToggleEvent,
            scale = 1F,
            isEnabled = isEnabled,
            buttonSize = DpSize(width = innerTextSize * 1.7F, height = innerTextSize)
        )
    }
}

//앱 정보 및 기능 설정 화면에서 사용할 박스 디자인
@Composable
fun SettingBoxItem(
    modifier: Modifier = Modifier,
    innerText: String,
    innerTextSize: Dp = 23.dp,
    onClick: (() -> Unit)? = null
) {


    val mutableInteractionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = mutableInteractionSource,
                        indication = null,
                    ) {
                        onClick()
                    }
                } else Modifier
            )
            .background(
                shape = RoundedCornerShape(50.dp),
                color = LocalColors.current.subSecondaryGray100.copy(alpha = 0.2F)
            )
            .padding(start = 20.dp, end = 10.dp)
            .padding(vertical = 10.dp)

    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = innerText,
            style = LocalTypography.current.sub01,
            fontSize = LocalDensity.current.run {
                (innerTextSize * 0.7F).toSp()
            }
        )

        Icon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(innerTextSize),
            imageVector = ProPoseIcon.ArrowForward,
            contentDescription = "화살표"
        )
    }

}
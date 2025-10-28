package com.hanadulset.pro_poseapp.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**Ripple 효과가 없는 클릭을 구현할 때 사용**/
@Composable
fun Modifier.noRippleClickable(
    onClick: () -> Unit
): Modifier = then(
    Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
)


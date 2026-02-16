package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//슬라이드 되는 애니메이션 적용할 컴포넌트
@Composable
fun AnimatedSlideToLeft(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    enterDuration: Int = 150,
    exitDuration: Int = 250,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInHorizontally(animationSpec = tween(
            durationMillis = enterDuration, easing = LinearOutSlowInEasing
        ), initialOffsetX = { fullHeight -> -fullHeight }).plus(fadeIn()),
        exit = slideOutHorizontally(animationSpec = tween(
            durationMillis = exitDuration, easing = LinearOutSlowInEasing
        ), targetOffsetX = { fullHeight -> -fullHeight }).plus(fadeOut()),
        content = content
    )
}

//슬라이드 되는 애니메이션 적용할 컴포넌트
@Composable
fun AnimatedSlideToRight(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    enterDuration: Int = 150,
    exitDuration: Int = 250,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInHorizontally(animationSpec = tween(
            durationMillis = enterDuration, easing = LinearOutSlowInEasing
        ), initialOffsetX = { fullHeight -> fullHeight }).plus(fadeIn()),
        exit = slideOutHorizontally(animationSpec = tween(
            durationMillis = exitDuration, easing = LinearOutSlowInEasing
        ), targetOffsetX = { fullHeight -> fullHeight }).plus(fadeOut()),
        content = content
    )
}
@file:OptIn(ExperimentalAnimationApi::class)

package com.san.kir.core.compose_utils.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <S> FromBottomToBottomAnimContent(
    targetState: S,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = content,
        transitionSpec = {
            slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> fullHeight }
            ) with slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { fullHeight -> fullHeight }
            )
        }
    )
}

@Composable
fun <S> FromEndToEndAnimContent(
    targetState: S,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = content,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(400),
                initialOffsetX = { fullWidth -> fullWidth }
            ) with slideOutHorizontally(
                animationSpec = tween(200),
                targetOffsetX = { fullWidth -> fullWidth }
            )
        }
    )
}

@Composable
inline fun BottomAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    noinline content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        content = content,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
    )
}
@Composable
inline fun ColumnScope.BottomAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    noinline content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        content = content,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
    )
}

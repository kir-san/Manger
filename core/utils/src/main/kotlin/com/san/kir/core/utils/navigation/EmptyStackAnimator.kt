package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.Direction
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.StackAnimator

object EmptyStackAnimator : StackAnimator {

    @Composable
    override operator fun invoke(
        direction: Direction,
        isInitial: Boolean,
        onFinished: () -> Unit,
        content: @Composable (Modifier) -> Unit
    ) {
        content(Modifier)

        DisposableEffect(direction, isInitial) {
            onFinished()
            onDispose {}
        }
    }
}

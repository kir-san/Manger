package com.san.kir.library.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.san.kir.library.ui.library.SelectedMangaState

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BottomDialog(
    state: SelectedMangaState,
    onDismiss: () -> Unit,
    dialogContent: @Composable AnimatedVisibilityScope.(SelectedMangaState.Visible) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val transition = updateTransition(targetState = state, "BottomDialogState")

    val color by transition.animateColor(label = "BottomDialogColor") { currentState ->
        when (currentState) {
            SelectedMangaState.NonVisible -> Color.Transparent
            is SelectedMangaState.Visible -> Color(0x66000000)
        }
    }

    BoxWithConstraints(modifier) {

        Box(modifier = Modifier.fillMaxSize()) {
            content()

            val dismissModifier =
                when (state) {
                    SelectedMangaState.NonVisible -> Modifier
                    is SelectedMangaState.Visible ->
                        Modifier.pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
                }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .then(dismissModifier)
            )
        }

        transition.AnimatedContent(
            transitionSpec = {
                slideInVertically(
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                    )
                ) { it } with slideOutVertically { it } using
                        SizeTransform { _, targetSize ->
                            when (targetState) {
                                SelectedMangaState.NonVisible ->
                                    keyframes {
                                        IntSize(targetSize.width, targetSize.height) at 300
                                    }
                                is SelectedMangaState.Visible ->
                                    keyframes {}
                            }

                        }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colors.surface,
                elevation = ModalBottomSheetDefaults.Elevation,
                contentColor = MaterialTheme.colors.onSurface,
                shape = MaterialTheme.shapes.large
            ) {
                when (it) {
                    SelectedMangaState.NonVisible -> {}
                    is SelectedMangaState.Visible -> dialogContent(it)
                }
            }
        }
    }
}

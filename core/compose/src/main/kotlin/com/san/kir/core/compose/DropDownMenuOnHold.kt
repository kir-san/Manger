package com.san.kir.core.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RemoveItemMenuOnHold(
    removeText: String,
    cancelText: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    fillingColor: Color = Color(0x6DFF0000),
    content: @Composable DropDownMenuOnHoldScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val scope = remember { DropDownMenuOnHoldScopeImpl(coroutineScope, onSuccess) }

    BoxWithConstraints(
        modifier = modifier
    ) {
        scope.maxWidth = maxWidth

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalInsetsPadding()
                .onSizeChanged { scope.maxHeight = it.height }
        ) {
            scope.content()

            BottomAnimatedVisibility(visible = scope.expandMenuState) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        text = removeText,
                        modifier = Modifier
                            .padding(Dimensions.small)
                            .weight(1f)
                            .holdPress(
                                onDown = scope::startAnimation,
                                onUp = scope::stopAnimation
                            ),
                        borderColor = fillingColor,
                    )

                    OutlinedButton(
                        onClick = scope::hideMenu,
                        modifier = Modifier
                            .padding(Dimensions.small)
                            .weight(1f)
                    ) {
                        Text(cancelText)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .then(with(LocalDensity.current) { Modifier.height(scope.maxHeight.toDp()) })
                .background(fillingColor)
                .width(scope.animator.value)
        )
    }
}

interface DropDownMenuOnHoldScope {
    fun Modifier.onClick(onClick: () -> Unit): Modifier
    fun showMenu()
    fun hideMenu()
}

@OptIn(ExperimentalFoundationApi::class)
class DropDownMenuOnHoldScopeImpl(
    private val coroutineScope: CoroutineScope,
    private val onSuccess: () -> Unit
) : DropDownMenuOnHoldScope {

    var expandMenuState by mutableStateOf(false)
        private set

    val animator = Animatable(0.dp, Dp.VectorConverter)
    var maxWidth by mutableStateOf(0.dp)
    var maxHeight by mutableStateOf(0)

    override fun Modifier.onClick(onClick: () -> Unit) =
        then(
            Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { expandMenuState = true })
        )

    override fun showMenu() {
        expandMenuState = true
    }

    override fun hideMenu() {
        expandMenuState = false
    }

    fun startAnimation() {
        coroutineScope.launch {
            if (animator
                    .animateTo(maxWidth, TweenSpec(2000, 0, LinearEasing))
                    .endReason == AnimationEndReason.Finished) {
                expandMenuState = false
                animator.snapTo(0.dp)
                onSuccess()
            }
        }
    }

    fun stopAnimation() {
        coroutineScope.launch {
            if (animator.isRunning) {
                animator.stop()
                animator.animateTo(0.dp, TweenSpec(500))
            }
        }
    }
}

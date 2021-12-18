package com.san.kir.ui.viewer

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener

internal fun animate(
    onUpdate: (Float) -> Unit,
    onStart: (Animator) -> Unit = {},
    onEnd: (Animator) -> Unit = {},
) {
    val animator = ValueAnimator.ofFloat(0f, 200f)
    animator.duration = 200L
    animator.interpolator = DecelerateInterpolator()

    animator.addUpdateListener { onUpdate(it.animatedValue as Float) }
    animator.addListener(
        onEnd = onEnd,
        onStart = onStart
    )

    animator.start()
}

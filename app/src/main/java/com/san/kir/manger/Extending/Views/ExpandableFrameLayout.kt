package com.san.kir.manger.Extending.Views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.san.kir.manger.Extending.Views.ExpandableFrameLayout.State.COLLAPSED
import com.san.kir.manger.Extending.Views.ExpandableFrameLayout.State.COLLAPSING
import com.san.kir.manger.Extending.Views.ExpandableFrameLayout.State.EXPANDED
import com.san.kir.manger.Extending.Views.ExpandableFrameLayout.State.EXPANDING


open class ExpandableFrameLayout(context: Context, attrs: AttributeSet? = null) :
        FrameLayout(context, attrs) {

    var duration = DEFAULT_DURATION
    private var parallax: Float = 0.toFloat()
    private var expansion: Float = 0.toFloat()
    private var orientation: Int = 1

    var state: Int = 0
        private set

    private var interpolator: Interpolator = FastOutSlowInInterpolator()
    private var animator: ValueAnimator? = null

    private var listener: OnExpansionUpdateListener? = null

    var isExpanded: Boolean
        get() = state == EXPANDING || state == EXPANDED
        set(expand) = setExpanded(expand, true)

    object State {
        const val COLLAPSED = 0
        const val COLLAPSING = 1
        const val EXPANDING = 2
        const val EXPANDED = 3
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()

        expansion = (if (isExpanded) 1 else 0).toFloat()

        bundle.putFloat(KEY_EXPANSION, expansion)
        bundle.putParcelable(KEY_SUPER_STATE, superState)

        return bundle
    }

    override fun onRestoreInstanceState(parcelable: Parcelable) {
        val bundle = parcelable as Bundle
        expansion = bundle.getFloat(KEY_EXPANSION)
        state = (if (expansion == 1f) EXPANDED else COLLAPSED).toInt()
        val superState = bundle.getParcelable<Parcelable>(KEY_SUPER_STATE)

        super.onRestoreInstanceState(superState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        val height = measuredHeight

        val size = if (orientation == LinearLayout.HORIZONTAL) width else height

        visibility = if (expansion == 0f && size == 0) GONE else View.VISIBLE

        val expansionDelta = size - Math.round(size * expansion)
        if (parallax > 0) {
            val parallaxDelta = expansionDelta * parallax
            (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    if (orientation == HORIZONTAL) {
                        it.translationX = parallaxDelta
                    } else {
                        it.translationY = -parallaxDelta
                    }
                }
        }

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(width - expansionDelta, height)
        } else {
            setMeasuredDimension(width, height - expansionDelta)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (animator != null) {
            animator!!.cancel()
        }
        super.onConfigurationChanged(newConfig)
    }

    fun toggle(animate: Boolean = true) {
        if (isExpanded) {
            collapse(animate)
        } else {
            expand(animate)
        }
    }

    fun expand(animate: Boolean = true) {
        setExpanded(true, animate)
    }

    fun collapse(animate: Boolean = true) {
        setExpanded(false, animate)
    }

    fun setExpanded(expand: Boolean, animate: Boolean) {
        if (expand == isExpanded) {
            return
        }

        val targetExpansion = if (expand) 1 else 0
        if (animate) {
            animateSize(targetExpansion)
        } else {
            setExpansion(targetExpansion.toFloat())
        }
    }

    fun setInterpolator(interpolator: Interpolator) {
        this.interpolator = interpolator
    }

    fun getExpansion(): Float {
        return expansion
    }

    fun setExpansion(expansion: Float) {
        if (this.expansion == expansion) {
            return
        }

        // Infer state from previous value
        val delta = expansion - this.expansion
        when {
            expansion == 0f -> state = COLLAPSED
            expansion == 1f -> state = EXPANDED
            delta < 0 -> state = COLLAPSING
            delta > 0 -> state = EXPANDING
        }

        visibility = if (state == COLLAPSED) GONE else View.VISIBLE
        this.expansion = expansion
        requestLayout()

        if (listener != null) {
            listener!!.onExpansionUpdate(expansion, state)
        }
    }

    fun getParallax(): Float {
        return parallax
    }

    fun setParallax(parallax: Float) {
        var parallax = parallax
        // Make sure parallax is between 0 and 1
        parallax = Math.min(1f, Math.max(0f, parallax))
        this.parallax = parallax
    }

    fun getOrientation(): Int {
        return orientation
    }

    fun setOrientation(orientation: Int) {
        if (orientation < 0 || orientation > 1) {
            throw IllegalArgumentException("Orientation must be either 0 (horizontal) or 1 (vertical)")
        }
        this.orientation = orientation
    }

    fun setOnExpansionUpdateListener(listener: OnExpansionUpdateListener) {
        this.listener = listener
    }

    private fun animateSize(targetExpansion: Int) {
        if (animator != null) {
            animator!!.cancel()
            animator = null
        }

        animator = ValueAnimator.ofFloat(expansion, targetExpansion.toFloat())
        animator!!.interpolator = interpolator
        animator!!.duration = duration.toLong()

        animator!!.addUpdateListener { valueAnimator -> setExpansion(valueAnimator.animatedValue as Float) }

        animator!!.addListener(ExpansionListener(targetExpansion))

        animator!!.start()
    }

    interface OnExpansionUpdateListener {
        fun onExpansionUpdate(expansionFraction: Float, state: Int)
    }

    private inner class ExpansionListener(private val targetExpansion: Int) :
        Animator.AnimatorListener {
        private var canceled: Boolean = false

        override fun onAnimationStart(animation: Animator) {
            state = if (targetExpansion == 0) COLLAPSING else EXPANDING
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!canceled) {
                state = (if (targetExpansion == 0) COLLAPSED else EXPANDED).toInt()
                setExpansion(targetExpansion.toFloat())
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            canceled = true
        }

        override fun onAnimationRepeat(animation: Animator) {}
    }

    companion object {

        const val KEY_SUPER_STATE = "super_state"
        const val KEY_EXPANSION = "expansion"

        const val HORIZONTAL = 0
        const val VERTICAL = 1

        private const val DEFAULT_DURATION = 300
    }
}

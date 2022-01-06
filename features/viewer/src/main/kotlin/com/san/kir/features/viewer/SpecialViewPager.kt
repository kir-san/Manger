package com.san.kir.features.viewer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

// Viewpager с возможностью отключать управление свайпами
class SpecialViewPager : ViewPager {

    private var isSwapable: Boolean = false

    constructor(context: Context) : super(context) {
        isSwapable = false
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        isSwapable = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (isSwapable)
            return try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                false
            }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (isSwapable)
            return super.onTouchEvent(ev)
        performClick()
        return false
    }

    fun setSwipable(isLocked: Boolean) {
        this.isSwapable = isLocked
    }

}

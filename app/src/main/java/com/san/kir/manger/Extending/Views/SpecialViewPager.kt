package com.san.kir.manger.Extending.Views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class SpecialViewPager : ViewPager {

    private var isLocked: Boolean = false

    constructor(context: Context) : super(context) {
        isLocked = false
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        isLocked = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isLocked)
            return try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                false
            }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (!isLocked)
            return super.onTouchEvent(ev)
        performClick()
        return false
    }

    fun setLocked(isLocked: Boolean) {
        this.isLocked = isLocked
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}

package com.san.kir.manger.extending.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent

class SpecialRecyclerView(context: Context) : RecyclerView(context) {
    private var isLocked: Boolean = false

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        if (!isLocked)
            return try {
                super.onInterceptTouchEvent(e)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                false
            }
        return false
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (!isLocked)
            return super.onTouchEvent(e)
        performClick()
        return false
    }

    fun setLocked(isLocked: Boolean) {
        this.isLocked = isLocked
    }
}

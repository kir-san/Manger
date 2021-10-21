package com.san.kir.manger.utils.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.ViewPager
import kotlin.math.min

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

}

fun recordInitialPaddingForView(view: View) =
    Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)


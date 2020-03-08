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

class RoundedImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        drawable?.let { drawable ->
            if (width == 0 || height == 0) return
            val b = (drawable as BitmapDrawable).bitmap
            val bitmap = b.copy(Bitmap.Config.ARGB_8888, true)

            val roundBitmap = getCroppedBitmap(bitmap, width)
            canvas.drawBitmap(roundBitmap, 0F, 0F, null)
        }
    }

    private fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
        val sbmp = if (bmp.width != radius || bmp.height != radius) {
            val smallest: Float = min(bmp.width, bmp.height).toFloat()
            val factor = smallest / radius
            Bitmap.createScaledBitmap(
                bmp,
                (bmp.width / factor).toInt(),
                (bmp.height / factor).toInt(),
                false
            )
        } else {
            bmp
        }

        val output = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = "#BAB399"
        val paint = Paint()
        val rect = Rect(0, 0, radius, radius)

        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.parseColor(color)
        canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f, radius / 2 + 0.1f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(sbmp, rect, rect, paint)

        return output
    }
}

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

class SquareImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

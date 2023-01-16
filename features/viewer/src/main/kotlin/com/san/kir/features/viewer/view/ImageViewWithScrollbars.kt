package com.san.kir.features.viewer.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

// Добавлена поддержка скроллов
class ImageViewWithScrollbars @JvmOverloads constructor(
    context: Context?,
    attr: AttributeSet? = null,
) : SubsamplingScaleImageView(context, attr) {
    private val debugTextPaint: Paint = Paint().apply {
        color = 0x80808080.toInt()
        style = Paint.Style.FILL
    }
    private var scrollbarsWidth = 30
    private var scrollbarRadius = 15f
    private var scrollbarsVisibility = true
    private val visibleRect = Rect()

    fun setScrollBarsWidth(width: Int) {
        scrollbarsWidth = width
        invalidate()
    }

    fun setScrollbarRadius(radius: Float) {
        scrollbarRadius = radius
        invalidate()
    }

    fun setScrollbarsVisible(visibility: Boolean) {
        scrollbarsVisibility = visibility
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw before image is ready so it doesn't move around during setup.
        if (!isReady) return

        if (scrollbarsVisibility.not()) return

        visibleFileRect(visibleRect)
        if (visibleRect.height() < sHeight - 2) {
            val scaleFactorV = height / sHeight.toFloat()
            canvas.drawRoundRect(
                (right - scrollbarsWidth).toFloat(),
                visibleRect.top * scaleFactorV,
                right.toFloat(),
                visibleRect.bottom * scaleFactorV,
                scrollbarRadius,
                scrollbarRadius,
                debugTextPaint
            )
        }

        if (visibleRect.width() < sWidth - 2) {
            val scaleFactorH = width / sWidth.toFloat()
            canvas.drawRoundRect(
                visibleRect.left * scaleFactorH,
                (bottom - scrollbarsWidth).toFloat(),
                visibleRect.right * scaleFactorH,
                bottom.toFloat(),
                scrollbarRadius,
                scrollbarRadius,
                debugTextPaint
            )
        }
    }
}

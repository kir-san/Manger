package com.san.kir.manger.Extending.Views

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class DiagramForManga(context: Context) : View(context) {

    private val _all = MutableLiveData<Long>()

    companion object {
        val YELLOW = Color.parseColor("#FFFF00")
        val BLUE = Color.parseColor("#00E5FF")
        val GRAY = Color.parseColor("#BDBDBD")
    }

    private val mPaint = Paint()
    private val oval = RectF()

    private var mAll = 0L
    private var mManga = 0L
    private var mRead = 0L

    private var isFirst = false

    private val defaultStartAngle = 310f
    private val defaultAngle = 0f

    private var mangaStartAngle = defaultStartAngle
    private var mangaAngle = defaultAngle

    private var readStartAngle = 0f
    private var readAngle = 0f

    override fun onDraw(canvas: Canvas) {

        val height = canvas.height.toFloat()
        val width = canvas.width.toFloat()

        val minSize = minOf(height, width)
        val radius = minSize / 2f - 60f
        val mangaOffset = minSize / 2f - 30f
        val readOffset = minSize / 2f - 20f

        val centerX = width / 2f
        val centerY = height / 2f

        mPaint.isAntiAlias = true

        mPaint.color = Color.TRANSPARENT
        canvas.drawPaint(mPaint)

        // контур основного круга
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.BLACK
        canvas.drawCircle(centerX, centerY, radius + 4f, mPaint)

        // основной круг
        mPaint.color = YELLOW // Желтый
        canvas.drawCircle(centerX, centerY, radius, mPaint)

        // контур текущего круга
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 7f
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.color = Color.BLACK

        oval.set(centerX - mangaOffset,
                 centerY - mangaOffset,
                 centerX + mangaOffset,
                 centerY + mangaOffset)

        canvas.drawArc(oval, mangaStartAngle, mangaAngle, true, mPaint)


        // текущий круг
        mPaint.style = Paint.Style.FILL
        mPaint.color = BLUE // Голубой

        oval.set(centerX - mangaOffset,
                 centerY - mangaOffset,
                 centerX + mangaOffset,
                 centerY + mangaOffset)

        canvas.drawArc(oval, mangaStartAngle, mangaAngle, true, mPaint)

        if (mRead > 0) {
            // контур круга прочитанного
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 7f
            mPaint.strokeJoin = Paint.Join.ROUND
            mPaint.strokeCap = Paint.Cap.ROUND
            mPaint.color = Color.BLACK

            oval.set(centerX - readOffset,
                     centerY - readOffset,
                     centerX + readOffset,
                     centerY + readOffset)

            canvas.drawArc(oval, readStartAngle, readAngle, true, mPaint)


            // круг прочитанного
            mPaint.style = Paint.Style.FILL
            mPaint.color = GRAY // Серый

            oval.set(centerX - readOffset,
                     centerY - readOffset,
                     centerX + readOffset,
                     centerY + readOffset)

            canvas.drawArc(oval, readStartAngle, readAngle, true, mPaint)
        }

        super.onDraw(canvas)

        launch(UI) {
            if (!isFirst) {
                isFirst = true
                delay(500L)
                calculate()
            }
        }
    }

    private fun calculate() {
        if (mManga > 0) {
            if (mAll == 0L) {
                mAll = mManga
            }

            mangaAngle = 360f * mManga / mAll
            mangaStartAngle = defaultStartAngle - (mangaAngle / 2f)

            if (mRead > 0) {
                readAngle = mangaAngle * mRead / mManga
                readStartAngle = mangaStartAngle + mangaAngle - readAngle
            }

            invalidate()
        }
    }

    fun setData(all: Long = mAll, manga: Long = mManga, read: Long = mRead) {
        mAll = all
        mManga = manga
        mRead = read
        isFirst = false
        calculate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

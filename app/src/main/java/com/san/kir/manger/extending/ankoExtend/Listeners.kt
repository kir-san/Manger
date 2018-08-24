package com.san.kir.manger.extending.ankoExtend

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView


fun SubsamplingScaleImageView.onDoubleTapListener(init: __IPhotoView_OnDoubleTapListener.() -> Unit) {
    val listener = __IPhotoView_OnDoubleTapListener()
    listener.init()

    val gestureDetector = GestureDetector(this.context, listener)

    setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
    }
}

@Suppress("ClassName")
class __IPhotoView_OnDoubleTapListener : android.view.GestureDetector.SimpleOnGestureListener() {
    private var _onDoubleTap: ((android.view.MotionEvent) -> Boolean)? = null
    private var _onDoubleTapEvent: ((android.view.MotionEvent) -> Boolean)? = null
    private var _onSingleTapConfirmed: ((android.view.MotionEvent) -> Boolean)? = null

    override fun onDoubleTap(e: android.view.MotionEvent) = _onDoubleTap?.invoke(e) == true

    fun onDoubleTap(listener: (android.view.MotionEvent) -> Boolean) {
        _onDoubleTap = listener
    }

    override fun onDoubleTapEvent(e: android.view.MotionEvent) = _onDoubleTapEvent?.invoke(e) == true

    fun onDoubleTapEvent(listener: (android.view.MotionEvent) -> Boolean) {
        _onDoubleTapEvent = listener
    }

    override fun onSingleTapConfirmed(e: android.view.MotionEvent) = _onSingleTapConfirmed?.invoke(e) == true

    fun onSingleTapConfirmed(listener: (android.view.MotionEvent) -> Boolean) {
        _onSingleTapConfirmed = listener
    }

}

interface ClickListener {
    fun onClick(view: View, position: Int)
    fun onLongClick(view: View, position: Int)
}

class RecyclerViewTouchListeners(context: Context,
                                 recyclerView: RecyclerView,
                                 val clickListener: ClickListener) : RecyclerView.SimpleOnItemTouchListener() {
    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?) = true
            override fun onLongPress(e: MotionEvent) {
                recyclerView.findChildViewUnder(e.x, e.y)?.let {
                    clickListener.onLongClick(it, recyclerView.getChildAdapterPosition(it))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        rv.findChildViewUnder(e.x, e.y)?.let {
            if (gestureDetector.onTouchEvent(e))
            clickListener.onClick(it, rv.getChildAdapterPosition(it))
        }
        return false
    }
}

fun RecyclerView.onClick(click: (View, Int) -> Unit) {
    val listener = object : ClickListener {
        override fun onClick(view: View, position: Int) {
            click.invoke(view, position)
        }

        override fun onLongClick(view: View, position: Int) {
        }
    }
    addOnItemTouchListener(RecyclerViewTouchListeners(context, this, listener))
}

fun RecyclerView.onScroll(scroll: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        var mScrolled = false

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE && mScrolled) {
                mScrolled = false
                scroll.invoke()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            if (dx != 0 || dy != 0) {
                mScrolled = true
            }
        }
    })
}



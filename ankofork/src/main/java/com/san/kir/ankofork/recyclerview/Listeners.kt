package com.san.kir.ankofork.recyclerview

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView


@Suppress("unused")
fun RecyclerView.onItemTouchListener(init: __RecyclerView_OnItemTouchListener.() -> Unit) {
    val listener = __RecyclerView_OnItemTouchListener()
    listener.init()
    addOnItemTouchListener(listener)
}

@Suppress("ClassName", "unused")
class __RecyclerView_OnItemTouchListener : RecyclerView.OnItemTouchListener {

    private var _onInterceptTouchEvent: ((RecyclerView, MotionEvent) -> Boolean)? = null

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent) = _onInterceptTouchEvent?.invoke(rv, e) ?: false

    fun onInterceptTouchEvent(listener: (RecyclerView, MotionEvent) -> Boolean) {
        _onInterceptTouchEvent = listener
    }

    private var _onTouchEvent: ((RecyclerView, MotionEvent) -> Unit)? = null

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        _onTouchEvent?.invoke(rv, e)
    }

    fun onTouchEvent(listener: (RecyclerView, MotionEvent) -> Unit) {
        _onTouchEvent = listener
    }

    private var _onRequestDisallowInterceptTouchEvent: ((Boolean) -> Unit)? = null

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        _onRequestDisallowInterceptTouchEvent?.invoke(disallowIntercept)
    }

    fun onRequestDisallowInterceptTouchEvent(listener: (Boolean) -> Unit) {
        _onRequestDisallowInterceptTouchEvent = listener
    }

}


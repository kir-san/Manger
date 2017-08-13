package com.san.kir.manger.Extending.AnkoExtend

fun com.san.kir.manger.photoview.IPhotoView.onDoubleTapListener(init: __IPhotoView_OnDoubleTapListener.() -> Unit) {
    val listener = __IPhotoView_OnDoubleTapListener()
    listener.init()
    setOnDoubleTapListener(listener)
}

class __IPhotoView_OnDoubleTapListener: android.view.GestureDetector.OnDoubleTapListener {
    private var _onDoubleTap: ((android.view.MotionEvent) -> Boolean)? = null
    private var _onDoubleTapEvent: ((android.view.MotionEvent) -> Boolean)? = null
    private var _onSingleTapConfirmed: ((android.view.MotionEvent) -> Boolean)? = null

    override fun onDoubleTap(e: android.view.MotionEvent) = _onDoubleTap?.invoke(e) ?: false

    fun onDoubleTap(listener: (android.view.MotionEvent) -> Boolean) {
        _onDoubleTap = listener
    }

    override fun onDoubleTapEvent(e: android.view.MotionEvent) = _onDoubleTapEvent?.invoke(e) ?: false

    fun onDoubleTapEvent(listener: (android.view.MotionEvent) -> Boolean) {
        _onDoubleTapEvent = listener
    }

    override fun onSingleTapConfirmed(e: android.view.MotionEvent) = _onSingleTapConfirmed?.invoke(e) ?: false

    fun onSingleTapConfirmed(listener: (android.view.MotionEvent) -> Boolean) {
        _onSingleTapConfirmed = listener
    }

}

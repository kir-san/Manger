package com.san.kir.manger.utils.extensions

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.san.kir.ankofork.subsampling_scale_image_view.SubsamplingScaleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


fun SubsamplingScaleImageView.onDoubleTapListener(init: __IPhotoView_OnDoubleTapListener.() -> Unit) {
    val listener = __IPhotoView_OnDoubleTapListener()
    listener.init()

    val gestureDetector = GestureDetectorCompat(this.context, listener)

    setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
    }
}

@Suppress("ClassName", "unused")
class __IPhotoView_OnDoubleTapListener : GestureDetector.SimpleOnGestureListener() {
    private var _onDoubleTap: ((MotionEvent) -> Boolean)? = null
    private var _onDoubleTapEvent: ((MotionEvent) -> Boolean)? = null
    private var _onSingleTapConfirmed: ((MotionEvent) -> Boolean)? = null

    override fun onDoubleTap(e: MotionEvent) = _onDoubleTap?.invoke(e) == true

    fun onDoubleTap(listener: (MotionEvent) -> Boolean) {
        _onDoubleTap = listener
    }

    override fun onDoubleTapEvent(e: MotionEvent) = _onDoubleTapEvent?.invoke(e) == true

    fun onDoubleTapEvent(listener: (MotionEvent) -> Boolean) {
        _onDoubleTapEvent = listener
    }

    override fun onSingleTapConfirmed(e: MotionEvent) = _onSingleTapConfirmed?.invoke(e) == true

    fun onSingleTapConfirmed(listener: (MotionEvent) -> Boolean) {
        _onSingleTapConfirmed = listener
    }

}

interface ClickListener {
    fun onClick(view: View, position: Int)
    fun onLongClick(view: View, position: Int)
}

class RecyclerViewTouchListeners(
    context: Context,
    recyclerView: androidx.recyclerview.widget.RecyclerView,
    val clickListener: ClickListener
) : androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener() {
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

    override fun onInterceptTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: MotionEvent): Boolean {
        rv.findChildViewUnder(e.x, e.y)?.let {
            if (gestureDetector.onTouchEvent(e))
                clickListener.onClick(it, rv.getChildAdapterPosition(it))
        }
        return false
    }
}

fun androidx.recyclerview.widget.RecyclerView.onClick(click: (View, Int) -> Unit) {
    val listener = object : ClickListener {
        override fun onClick(view: View, position: Int) {
            click.invoke(view, position)
        }

        override fun onLongClick(view: View, position: Int) = Unit
    }
    addOnItemTouchListener(
        RecyclerViewTouchListeners(
            context,
            this,
            listener
        )
    )
}

fun android.widget.CompoundButton.onCheckedChange(
    context: CoroutineContext = Dispatchers.Main,
    handler: suspend CoroutineScope.(buttonView: android.widget.CompoundButton?, isChecked: Boolean) -> Unit
) {
    setOnCheckedChangeListener { buttonView, isChecked ->
        GlobalScope.launch(context) {
            handler(buttonView, isChecked)
        }
    }
}

@Suppress("ClassName", "unused")
class SubsamplingScaleImageView_OnImageEventListener :
    SubsamplingScaleImageView.OnImageEventListener {
    private var _onReady: (() -> Unit)? = null
    private var _onImageLoaded: (() -> Unit)? = null
    private var _onPreviewReleased: (() -> Unit)? = null
    private var _onPreviewLoadError: ((Exception) -> Unit)? = null
    private var _onImageLoadError: ((Exception) -> Unit)? = null
    private var _onTileLoadError: ((Exception) -> Unit)? = null

    override fun onReady() {
        _onReady?.invoke()
    }

    fun onReady(listener: () -> Unit) {
        _onReady = listener
    }

    override fun onImageLoaded() {
        _onImageLoaded?.invoke()
    }

    fun onImageLoaded(listener: () -> Unit) {
        _onImageLoaded = listener
    }

    override fun onPreviewReleased() {
        _onPreviewReleased?.invoke()
    }

    fun onPreviewReleased(listener: () -> Unit) {
        _onPreviewReleased = listener
    }

    override fun onPreviewLoadError(e: Exception) {
        _onPreviewLoadError?.invoke(e)
    }

    fun onPreviewLoadError(listener: (Exception) -> Unit) {
        _onPreviewLoadError = listener
    }

    override fun onImageLoadError(e: Exception) {
        _onImageLoadError?.invoke(e)
    }

    fun onImageLoadError(listener: (Exception) -> Unit) {
        _onImageLoadError = listener
    }

    override fun onTileLoadError(e: Exception) {
        _onTileLoadError?.invoke(e)
    }

    fun onTileLoadError(listener: (Exception) -> Unit) {
        _onTileLoadError = listener
    }
}

@Suppress("unused")
fun SubsamplingScaleImageView.imageEventListener(init: SubsamplingScaleImageView_OnImageEventListener.() -> Unit) {
    val listener =
        SubsamplingScaleImageView_OnImageEventListener()
    listener.init()

    setOnImageEventListener(listener)
}

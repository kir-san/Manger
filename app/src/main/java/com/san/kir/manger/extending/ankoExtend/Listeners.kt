package com.san.kir.manger.extending.ankoExtend

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


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

fun android.widget.SeekBar.onSeekBarChangeListener(
    context: CoroutineDispatcher = Dispatchers.Main,
    init: __SeekBar_OnSeekBarChangeListener.() -> Unit
) {
    val listener = __SeekBar_OnSeekBarChangeListener(context)
    listener.init()
    setOnSeekBarChangeListener(listener)
}

@Suppress("ClassName")
class __SeekBar_OnSeekBarChangeListener(private val context: CoroutineDispatcher) : android.widget.SeekBar.OnSeekBarChangeListener {

    private var _onProgressChanged: (suspend CoroutineScope.(android.widget.SeekBar?, Int, Boolean) -> Unit)? = null


    override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
        val handler = _onProgressChanged ?: return
        GlobalScope.launch(context) {
            handler(seekBar, progress, fromUser)
        }
    }

    fun onProgressChanged(
        listener: suspend CoroutineScope.(android.widget.SeekBar?, Int, Boolean) -> Unit
    ) {
        _onProgressChanged = listener
    }

    private var _onStartTrackingTouch: (suspend CoroutineScope.(android.widget.SeekBar?) -> Unit)? = null


    override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
        val handler = _onStartTrackingTouch ?: return
        GlobalScope.launch(context) {
            handler(seekBar)
        }
    }

    fun onStartTrackingTouch(
        listener: suspend CoroutineScope.(android.widget.SeekBar?) -> Unit
    ) {
        _onStartTrackingTouch = listener
    }

    private var _onStopTrackingTouch: (suspend CoroutineScope.(android.widget.SeekBar?) -> Unit)? = null


    override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
        val handler = _onStopTrackingTouch ?: return
        GlobalScope.launch(context) {
            handler(seekBar)
        }
    }

    fun onStopTrackingTouch(
        listener: suspend CoroutineScope.(android.widget.SeekBar?) -> Unit
    ) {
        _onStopTrackingTouch = listener
    }

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

fun android.view.View.onClick(
    context: CoroutineContext = Dispatchers.Main,
    scope: CoroutineScope = GlobalScope,
    handler: suspend CoroutineScope.(v: android.view.View?) -> Unit
) {
    setOnClickListener { v ->
        scope.launch(context) {
            handler(v)
        }
    }
}

fun onClickListener(
    context: CoroutineContext = Dispatchers.Main,
    handler: suspend CoroutineScope.(v: android.view.View?) -> Unit
): View.OnClickListener {
    return View.OnClickListener { v ->
        GlobalScope.launch(context) {
            handler(v)
        }
    }
}

fun onLongClickListener(
    context: CoroutineContext = Dispatchers.Main,
    handler: suspend CoroutineScope.(v: android.view.View?) -> Unit
): View.OnLongClickListener {
    return View.OnLongClickListener { v ->
        GlobalScope.launch(context) {
            handler(v)
        }
        return@OnLongClickListener true
    }
}

fun android.widget.TextView.textChangedListener(
    context: CoroutineContext = Dispatchers.Main,
    init: __TextWatcher.() -> Unit
) {
    val listener = __TextWatcher(context)
    listener.init()
    addTextChangedListener(listener)
}

class __TextWatcher(private val context: CoroutineContext) : android.text.TextWatcher {

    private var _beforeTextChanged: (suspend CoroutineScope.(CharSequence?, Int, Int, Int) -> Unit)? = null


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        val handler = _beforeTextChanged ?: return
        GlobalScope.launch(context) {
            handler(s, start, count, after)
        }
    }

    fun beforeTextChanged(
        listener: suspend CoroutineScope.(CharSequence?, Int, Int, Int) -> Unit
    ) {
        _beforeTextChanged = listener
    }

    private var _onTextChanged: (suspend CoroutineScope.(CharSequence?, Int, Int, Int) -> Unit)? = null


    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val handler = _onTextChanged ?: return
        GlobalScope.launch(context) {
            handler(s, start, before, count)
        }
    }

    fun onTextChanged(
        listener: suspend CoroutineScope.(CharSequence?, Int, Int, Int) -> Unit
    ) {
        _onTextChanged = listener
    }

    private var _afterTextChanged: (suspend CoroutineScope.(android.text.Editable?) -> Unit)? = null


    override fun afterTextChanged(s: android.text.Editable?) {
        val handler = _afterTextChanged ?: return
        GlobalScope.launch(context) {
            handler(s)
        }
    }

    fun afterTextChanged(
        listener: suspend CoroutineScope.(android.text.Editable?) -> Unit
    ) {
        _afterTextChanged = listener
    }

}

fun android.view.View.onLongClick(
    context: CoroutineContext = Dispatchers.Main,
    returnValue: Boolean = false,
    handler: suspend CoroutineScope.(v: android.view.View?) -> Unit
) {
    setOnLongClickListener { v ->
        GlobalScope.launch(context) {
            handler(v)
        }
        returnValue
    }
}

fun android.support.v7.widget.SearchView.onQueryTextListener(
    context: CoroutineContext = Dispatchers.Main,
    init: __QueryTextListener.() -> Unit
) {
    val listener = __QueryTextListener(context)
    listener.init()
    setOnQueryTextListener(listener)
}

class __QueryTextListener(private val context: CoroutineContext) : android.support.v7.widget.SearchView.OnQueryTextListener {

    private var _onQueryTextSubmit: (suspend CoroutineScope.(String?) -> Unit)? = null

    override fun onQueryTextSubmit(query: String?): Boolean {
        val handler = _onQueryTextSubmit ?: return false
        GlobalScope.launch(context) {
            handler(query)
        }
        return true
    }

    fun onQueryTextSubmit(
        listener: suspend CoroutineScope.(String?) -> Unit
    ) {
        _onQueryTextSubmit = listener
    }

    private var _onQueryTextChange: (suspend CoroutineScope.(String?) -> Unit)? = null

    override fun onQueryTextChange(newText: String?): Boolean {
        val handler = _onQueryTextChange ?: return false
        GlobalScope.launch(context) {
            handler(newText)
        }
        return true
    }

    fun onQueryTextChange(
        listener: suspend CoroutineScope.(String?) -> Unit
    ) {
        _onQueryTextChange = listener
    }
}

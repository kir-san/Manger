package com.san.kir.ankofork.sdk28

import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.widget.SearchView


fun TextView.textChangedListener(init: __TextWatcher.() -> Unit) {
    val listener = __TextWatcher()
    listener.init()
    addTextChangedListener(listener)
}

@Suppress("ClassName", "unused")
class __TextWatcher : android.text.TextWatcher {

    private var _beforeTextChanged: ((CharSequence?, Int, Int, Int) -> Unit)? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        _beforeTextChanged?.invoke(s, start, count, after)
    }

    fun beforeTextChanged(listener: (CharSequence?, Int, Int, Int) -> Unit) {
        _beforeTextChanged = listener
    }

    private var _onTextChanged: ((CharSequence?, Int, Int, Int) -> Unit)? = null

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        _onTextChanged?.invoke(s, start, before, count)
    }

    fun onTextChanged(listener: (CharSequence?, Int, Int, Int) -> Unit) {
        _onTextChanged = listener
    }

    private var _afterTextChanged: ((android.text.Editable?) -> Unit)? = null

    override fun afterTextChanged(s: android.text.Editable?) {
        _afterTextChanged?.invoke(s)
    }

    fun afterTextChanged(listener: (android.text.Editable?) -> Unit) {
        _afterTextChanged = listener
    }

}

inline fun View.onClick(noinline l: (v: View?) -> Unit) {
    setOnClickListener(l)
}

@Suppress("unused")
inline fun View.onLongClick(noinline l: (v: View?) -> Boolean) {
    setOnLongClickListener(l)
}

@Suppress("unused")
inline fun CompoundButton.onCheckedChange(noinline l: (buttonView: CompoundButton?, isChecked: Boolean) -> Unit) {
    setOnCheckedChangeListener(l)
}

@Suppress("unused")
inline fun RadioGroup.onCheckedChange(noinline l: (group: RadioGroup?, checkedId: Int) -> Unit) {
    setOnCheckedChangeListener(l)
}

fun SearchView.onQueryTextListener(init: __SearchView_OnQueryTextListener.() -> Unit) {
    val listener = __SearchView_OnQueryTextListener()
    listener.init()
    setOnQueryTextListener(listener)
}

@Suppress("ClassName", "unused")
class __SearchView_OnQueryTextListener : SearchView.OnQueryTextListener {

    private var _onQueryTextSubmit: ((String?) -> Boolean)? = null

    override fun onQueryTextSubmit(query: String?) = _onQueryTextSubmit?.invoke(query) ?: false

    fun onQueryTextSubmit(listener: (String?) -> Boolean) {
        _onQueryTextSubmit = listener
    }

    private var _onQueryTextChange: ((String?) -> Boolean)? = null

    override fun onQueryTextChange(newText: String?) = _onQueryTextChange?.invoke(newText) ?: false

    fun onQueryTextChange(listener: (String?) -> Boolean) {
        _onQueryTextChange = listener
    }

}

fun SeekBar.onSeekBarChangeListener(init: __SeekBar_OnSeekBarChangeListener.() -> Unit) {
    val listener = __SeekBar_OnSeekBarChangeListener()
    listener.init()
    setOnSeekBarChangeListener(listener)
}

@Suppress("ClassName", "unused")
class __SeekBar_OnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

    private var _onProgressChanged: ((SeekBar?, Int, Boolean) -> Unit)? = null

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        _onProgressChanged?.invoke(seekBar, progress, fromUser)
    }

    fun onProgressChanged(listener: (SeekBar?, Int, Boolean) -> Unit) {
        _onProgressChanged = listener
    }

    private var _onStartTrackingTouch: ((SeekBar?) -> Unit)? = null

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        _onStartTrackingTouch?.invoke(seekBar)
    }

    fun onStartTrackingTouch(listener: (SeekBar?) -> Unit) {
        _onStartTrackingTouch = listener
    }

    private var _onStopTrackingTouch: ((SeekBar?) -> Unit)? = null

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        _onStopTrackingTouch?.invoke(seekBar)
    }

    fun onStopTrackingTouch(listener: (SeekBar?) -> Unit) {
        _onStopTrackingTouch = listener
    }

}

@Suppress("unused")
inline fun TimePicker.onTimeChanged(noinline l: (view: TimePicker?, hourOfDay: Int, minute: Int) -> Unit) {
    setOnTimeChangedListener(l)
}


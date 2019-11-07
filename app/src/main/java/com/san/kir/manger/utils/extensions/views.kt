package com.san.kir.manger.utils.extensions

import android.content.DialogInterface
import android.graphics.Typeface
import android.text.InputType
import android.view.View
import android.view.ViewManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.ankoView
import com.san.kir.ankofork.bottomPadding
import com.san.kir.ankofork.dialogs.AlertBuilder
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.find
import com.san.kir.ankofork.sdk28.imageResource
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.topPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

inline fun ViewManager.specialViewPager(theme: Int = 0, init: SpecialViewPager.() -> Unit) =
    ankoView(::SpecialViewPager, theme, init)

inline fun ViewManager.bigImageView(init: SubsamplingScaleImageView.() -> Unit) =
    ankoView(::SubsamplingScaleImageView, 0) {
        init()
    }

inline fun ViewManager.squareFrameLayout(theme: Int = 0, init: _SquareFrameLayout.() -> Unit) =
    ankoView(::_SquareFrameLayout, theme) {
        init()
    }

inline fun ViewManager.roundedImageView(init: RoundedImageView.() -> Unit) =
    ankoView(::RoundedImageView, 0, init)

fun ViewManager.labelView(text: Int, init: TextView.() -> Unit = {}) = textView(text) {
    textSize = 14f
    bottomPadding = 0
    topPadding = dip(5)
    init()
}

fun ViewManager.textViewBold16Size(text: String = "", init: TextView.() -> Unit = {}): TextView {
    return textView(text) {
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
        init()
    }
}

fun ViewManager.textViewBold16Size(text: Int, init: TextView.() -> Unit = {}): TextView {
    return textView(text) {
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
        init()
    }
}

fun EditText.typeText(): EditText {
    inputType = InputType.TYPE_CLASS_TEXT
    return this
}

fun EditText.typeTextMultiLine(): EditText {
    inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
    return this
}


fun AlertBuilder<*>.positiveButton(
    buttonText: String,
    context: CoroutineContext = Dispatchers.Main,
    handler: suspend CoroutineScope.(dialog: DialogInterface) -> Unit
) {
    positiveButton(buttonText) { GlobalScope.launch(context) { handler(it) } }
}

fun AlertBuilder<*>.positiveButton(
    buttonText: Int,
    context: CoroutineContext = Dispatchers.Main,
    handler: suspend CoroutineScope.(dialog: DialogInterface) -> Unit
) {
    positiveButton(buttonText) { GlobalScope.launch(context) { handler(it) } }
}

fun View.visibleOrGone(isVisible: Binder<Boolean>) {
    isVisible.bind {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

fun View.visibleOrGone(isVisible1: Binder<Boolean>, isVisible2: Binder<Boolean>) {
    isVisible1.bind { arg1 ->
        isVisible2.bind { arg2 ->
            visibility = if (arg1 || arg2) View.VISIBLE else View.GONE
        }
    }
}

fun View.visibleOrGone(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.visibleOrInvisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}

fun View.goneOrVisible(isGone: Binder<Boolean>) {
    isGone.bind {
        visibility = if (it) View.GONE else View.VISIBLE
    }
}

fun View.invisibleOrVisible(isInvisible: Boolean) {
    visibility = if (isInvisible) View.INVISIBLE else View.VISIBLE
}

fun View.visibleOrInvisible(isInvisible: Binder<Boolean>) {
    isInvisible.bind {
        visibility = if (it) View.VISIBLE else View.INVISIBLE
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun SearchView.setButton(resId: Int) {
    find<ImageView>(androidx.appcompat.R.id.search_button).imageResource = resId
}

fun SearchView.setCloseButton(resId: Int) {
    find<ImageView>(androidx.appcompat.R.id.search_close_btn).imageResource = resId
}

fun SearchView.setTextColor(color: Int) {
    find<TextView>(androidx.appcompat.R.id.search_src_text).textColor = color
}






package com.san.kir.manger.Extending.AnkoExtend

import android.content.DialogInterface
import android.graphics.Typeface
import android.text.InputType
import android.view.View
import android.view.ViewManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.Views.SpecialViewPager
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import kotlin.coroutines.experimental.CoroutineContext

inline fun ViewManager.specialViewPager(theme: Int = 0, init: SpecialViewPager.() -> Unit) =
    ankoView(::SpecialViewPager, theme) {
        init()
    }

inline fun ViewManager.bigImageView(init: SubsamplingScaleImageView.() -> Unit) =
    ankoView(::SubsamplingScaleImageView, 0) {
        init()
    }


fun <T> bind(
    binder: Binder<T>,
    context: CoroutineContext = UI,
    binding: suspend (T) -> Unit
) = binder.bind(context, binding)


inline fun ViewManager.squareFrameLayout(theme: Int = 0, init: _SquareFrameLayout.() -> Unit) =
    ankoView(::_SquareFrameLayout, theme) {
        init()
    }

inline fun ViewManager.expandableFrameLayout(
    theme: Int = 0,
    init: _ExpandableFrameLayout.() -> Unit
) = ankoView(::_ExpandableFrameLayout, theme) {
    init()
}

inline fun ViewManager.textView(text: Binder<String>, init: TextView.() -> Unit) =
    textView {
        init()
        text.bind { setText(it) }
    }

fun ViewManager.labelView(text: String) = textView(text) {
    textSize = 10f
    bottomPadding = 0
    topPadding = dip(5)
}

fun ViewManager.textViewBold15Size(text: String): TextView {
    return textViewBold15Size(text) {}
}

fun ViewManager.textViewBold15Size(text: String, init: TextView.() -> Unit): TextView {
    return textView(text) {
        textSize = 15f
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


inline fun ViewManager.editText(text: Binder<String>, init: EditText.() -> Unit): EditText {
    return editText {
        text.bind { setText(it) }
        init()
    }
}

fun ViewManager.radioButton(
    id: Int = ID.generate(),
    text: Int,
    init: RadioButton.() -> Unit
): RadioButton {
    return radioButton {
        this.id = id
        if (text != 0) setText(text)
        init()
    }
}

fun AlertBuilder<*>.positiveButton(
    buttonText: String,
    context: CoroutineContext = UI,
    handler: suspend CoroutineScope.(dialog: DialogInterface) -> Unit
) {
    positiveButton(buttonText) { launch(context) { handler(it) } }
}

fun View.visibleOrGone(isVisible: Binder<Boolean>) {
    isVisible.bind {
        visibility = if (it) View.VISIBLE else View.GONE
    }
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





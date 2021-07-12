package com.san.kir.ankofork.sdk28

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import android.widget.TextView
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesSdk28View {
    val VIEW = { ctx: Context -> android.view.View(ctx) }
    val BUTTON = { ctx: Context -> android.widget.Button(ctx) }
    val CHECK_BOX = { ctx: Context -> android.widget.CheckBox(ctx) }
    val EDIT_TEXT = { ctx: Context -> android.widget.EditText(ctx) }
    val IMAGE_BUTTON = { ctx: Context -> android.widget.ImageButton(ctx) }
    val IMAGE_VIEW = { ctx: Context -> android.widget.ImageView(ctx) }
    val PROGRESS_BAR = { ctx: Context -> android.widget.ProgressBar(ctx) }
    val RADIO_BUTTON = { ctx: Context -> android.widget.RadioButton(ctx) }
    val SEEK_BAR = { ctx: Context -> android.widget.SeekBar(ctx) }
    val SPACE = { ctx: Context -> android.widget.Space(ctx) }
    val SPINNER = { ctx: Context -> android.widget.Spinner(ctx) }
    val SWITCH = { ctx: Context -> android.widget.Switch(ctx) }
    val TEXT_VIEW = { ctx: Context -> TextView(ctx) }
    val TIME_PICKER = { ctx: Context -> android.widget.TimePicker(ctx) }
}

fun ViewManager.view(): android.view.View = view {}
inline fun ViewManager.view(init: (@AnkoViewDslMarker android.view.View).() -> Unit): android.view.View {
    return ankoView(AnkoFactoriesSdk28View.VIEW, theme = 0) { init() }
}

inline fun ViewManager.button(
    text: CharSequence?,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        init()
        setText(text)
    }
}

fun ViewManager.button(text: Int): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.button(
    text: Int,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.checkBox(init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) { init() }
}


fun ViewManager.checkBox(text: Int): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        setText(text)
    }
}

fun ViewManager.editText(): android.widget.EditText = editText {}
inline fun ViewManager.editText(init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme = 0) { init() }
}

inline fun ViewManager.imageButton(init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme = 0) { init() }
}

fun ViewManager.imageView(): android.widget.ImageView = imageView {}
inline fun ViewManager.imageView(init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) { init() }
}

fun ViewManager.imageView(imageDrawable: android.graphics.drawable.Drawable?): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.imageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        init()
        setImageDrawable(imageDrawable)
    }
}

fun ViewManager.imageView(imageResource: Int): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.imageView(
    imageResource: Int,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        init()
        setImageResource(imageResource)
    }
}

fun ViewManager.progressBar(): android.widget.ProgressBar = progressBar {}
inline fun ViewManager.progressBar(init: (@AnkoViewDslMarker android.widget.ProgressBar).() -> Unit): android.widget.ProgressBar {
    return ankoView(AnkoFactoriesSdk28View.PROGRESS_BAR, theme = 0) { init() }
}

inline fun ViewManager.radioButton(init: (@AnkoViewDslMarker android.widget.RadioButton).() -> Unit): android.widget.RadioButton {
    return ankoView(AnkoFactoriesSdk28View.RADIO_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.seekBar(init: (@AnkoViewDslMarker android.widget.SeekBar).() -> Unit): android.widget.SeekBar {
    return ankoView(AnkoFactoriesSdk28View.SEEK_BAR, theme = 0) { init() }
}

fun ViewManager.space(): android.widget.Space = space {}
inline fun ViewManager.space(init: (@AnkoViewDslMarker android.widget.Space).() -> Unit): android.widget.Space {
    return ankoView(AnkoFactoriesSdk28View.SPACE, theme = 0) { init() }
}

fun ViewManager.spinner(): android.widget.Spinner = spinner {}
inline fun ViewManager.spinner(init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme = 0) { init() }
}

inline fun ViewManager.switch(init: (@AnkoViewDslMarker android.widget.Switch).() -> Unit): android.widget.Switch {
    return ankoView(AnkoFactoriesSdk28View.SWITCH, theme = 0) { init() }
}

fun ViewManager.textView(): TextView = textView {}
inline fun ViewManager.textView(init: (@AnkoViewDslMarker TextView).() -> Unit): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) { init() }
}

fun ViewManager.textView(text: CharSequence?): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.textView(
    text: CharSequence?,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.textView(text: Binder<String>, init: TextView.() -> Unit) =
    textView {
        init()
        text.bind { setText(it) }
    }

fun ViewManager.textView(text: Int): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.textView(
    text: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.timePicker(init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme = 0) { init() }
}

@PublishedApi
internal object AnkoFactoriesSdk28ViewGroup {
    val FRAME_LAYOUT = { ctx: Context -> _FrameLayout(ctx) }
    val LINEAR_LAYOUT = { ctx: Context -> _LinearLayout(ctx) }
    val RADIO_GROUP = { ctx: Context -> _RadioGroup(ctx) }
    val RELATIVE_LAYOUT = { ctx: Context -> _RelativeLayout(ctx) }
    val HORIZONTAL_SCROLLVIEW = { ctx: Context -> _HorizontalScrollView(ctx) }
    val SCROLLVIEW = { ctx: Context -> _ScrollView(ctx) }
}

inline fun ViewManager.frameLayout(init: (@AnkoViewDslMarker _FrameLayout).() -> Unit): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme = 0) { init() }
}

fun ViewManager.linearLayout(): android.widget.LinearLayout = linearLayout {}
inline fun ViewManager.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

fun Context.linearLayout(): android.widget.LinearLayout = linearLayout {}
inline fun Context.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

fun Activity.linearLayout(): android.widget.LinearLayout = linearLayout {}
inline fun Activity.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.radioGroup(init: (@AnkoViewDslMarker _RadioGroup).() -> Unit): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme = 0) { init() }
}

inline fun ViewManager.relativeLayout(init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme = 0) { init() }
}

inline fun Context.relativeLayout(init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.horizontalScrollView(init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLLVIEW, theme = 0) { init() }
}

inline fun ViewManager.scrollView(init: (@AnkoViewDslMarker _ScrollView).() -> Unit): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLLVIEW, theme = 0) { init() }
}

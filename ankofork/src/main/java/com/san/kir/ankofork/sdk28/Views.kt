package com.san.kir.ankofork.sdk28

import android.content.Context
import android.view.ViewManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesSdk28View {
    val VIEW = { ctx: Context -> android.view.View(ctx) }
    val BUTTON = { ctx: Context -> android.widget.Button(ctx) }
    val PROGRESS_BAR = { ctx: Context -> android.widget.ProgressBar(ctx) }
    val SPACE = { ctx: Context -> android.widget.Space(ctx) }
    val SWITCH = { ctx: Context -> android.widget.Switch(ctx) }
    val TEXT_VIEW = { ctx: Context -> TextView(ctx) }
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


inline fun ViewManager.progressBar(init: (@AnkoViewDslMarker android.widget.ProgressBar).() -> Unit): android.widget.ProgressBar {
    return ankoView(AnkoFactoriesSdk28View.PROGRESS_BAR, theme = 0) { init() }
}

fun ViewManager.space(): android.widget.Space = space {}
inline fun ViewManager.space(init: (@AnkoViewDslMarker android.widget.Space).() -> Unit): android.widget.Space {
    return ankoView(AnkoFactoriesSdk28View.SPACE, theme = 0) { init() }
}

inline fun ViewManager.switch(init: (@AnkoViewDslMarker android.widget.Switch).() -> Unit): android.widget.Switch {
    return ankoView(AnkoFactoriesSdk28View.SWITCH, theme = 0) { init() }
}

inline fun ViewManager.textView(init: (@AnkoViewDslMarker TextView).() -> Unit): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) { init() }
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

@PublishedApi
internal object AnkoFactoriesSdk28ViewGroup {
    val LINEAR_LAYOUT = { ctx: Context -> _LinearLayout(ctx) }
    val RELATIVE_LAYOUT = { ctx: Context -> _RelativeLayout(ctx) }
}

inline fun ViewManager.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

inline fun Context.relativeLayout(init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme = 0) { init() }
}

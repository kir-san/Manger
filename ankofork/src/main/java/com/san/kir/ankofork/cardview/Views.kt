package com.san.kir.ankofork.cardview

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import androidx.cardview.widget.CardView
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesCardviewV7ViewGroup {
    val CARD_VIEW = { ctx: Context -> _CardView(ctx) }
}

inline fun ViewManager.cardView(): CardView = cardView {}
inline fun ViewManager.cardView(init: (@AnkoViewDslMarker _CardView).() -> Unit): CardView {
    return ankoView(AnkoFactoriesCardviewV7ViewGroup.CARD_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedCardView(theme: Int = 0): CardView = themedCardView(theme) {}
inline fun ViewManager.themedCardView(theme: Int = 0, init: (@AnkoViewDslMarker _CardView).() -> Unit): CardView {
return ankoView(AnkoFactoriesCardviewV7ViewGroup.CARD_VIEW, theme) { init() }
}

inline fun Context.cardView(): CardView = cardView {}
inline fun Context.cardView(init: (@AnkoViewDslMarker _CardView).() -> Unit): CardView {
    return ankoView(AnkoFactoriesCardviewV7ViewGroup.CARD_VIEW, theme = 0) { init() }
}

inline fun Context.themedCardView(theme: Int = 0): CardView = themedCardView(theme) {}
inline fun Context.themedCardView(theme: Int = 0, init: (@AnkoViewDslMarker _CardView).() -> Unit): CardView {
return ankoView(AnkoFactoriesCardviewV7ViewGroup.CARD_VIEW, theme) { init() }
}

inline fun Activity.cardView(): CardView = cardView {}
inline fun Activity.cardView(init: (@AnkoViewDslMarker _CardView).() -> Unit): CardView {
    return ankoView(AnkoFactoriesCardviewV7ViewGroup.CARD_VIEW, theme = 0) { init() }
}

inline fun Activity.themedCardView(theme: Int = 0): CardView = themedCardView(theme) {}
inline fun Activity.themedCardView(theme: Int = 0, init: (@AnkoViewDslMarker _CardView).() -> Unit): CardView {
return ankoView(AnkoFactoriesCardviewV7ViewGroup.CARD_VIEW, theme) { init() }
}


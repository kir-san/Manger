package com.san.kir.ankofork.recyclerview

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesRecyclerviewV7ViewGroup {
    val RECYCLER_VIEW = { ctx: Context -> _RecyclerView(ctx) }
}

inline fun ViewManager.recyclerView(): RecyclerView = recyclerView {}
inline fun ViewManager.recyclerView(init: (@AnkoViewDslMarker _RecyclerView).() -> Unit): RecyclerView {
    return ankoView(AnkoFactoriesRecyclerviewV7ViewGroup.RECYCLER_VIEW, theme = 0) { init() }
}

inline fun Context.recyclerView(): RecyclerView = recyclerView {}
inline fun Context.recyclerView(init: (@AnkoViewDslMarker _RecyclerView).() -> Unit): RecyclerView {
    return ankoView(AnkoFactoriesRecyclerviewV7ViewGroup.RECYCLER_VIEW, theme = 0) { init() }
}

inline fun Activity.recyclerView(): RecyclerView = recyclerView {}
inline fun Activity.recyclerView(init: (@AnkoViewDslMarker _RecyclerView).() -> Unit): RecyclerView {
    return ankoView(AnkoFactoriesRecyclerviewV7ViewGroup.RECYCLER_VIEW, theme = 0) { init() }
}


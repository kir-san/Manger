
package com.san.kir.ankofork.appcompat

import android.content.Context
import android.view.ViewManager
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView


@PublishedApi
internal object AnkoFactoriesAppcompatV7ViewGroup {
    val TOOLBAR = { ctx: Context -> _Toolbar(ctx) }
}

inline fun ViewManager.toolbar(init: (@AnkoViewDslMarker _Toolbar).() -> Unit): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme = 0) { init() }
}


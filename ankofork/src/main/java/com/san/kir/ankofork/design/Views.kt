@file:JvmName("DesignViewsKt")
package com.san.kir.ankofork.design

import android.content.Context
import android.view.ViewManager
import com.google.android.material.appbar.AppBarLayout
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesDesignViewGroup {
    val APP_BAR_LAYOUT = { ctx: Context -> _AppBarLayout(ctx) }
}

inline fun ViewManager.themedAppBarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme) { init() }
}


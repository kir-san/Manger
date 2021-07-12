package com.san.kir.ankofork.support

import android.content.Context
import android.view.ViewManager
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesSupportV4ViewGroup {
    val VIEW_PAGER = { ctx: Context -> _ViewPager(ctx) }
    val DRAWER_LAYOUT = { ctx: Context -> _DrawerLayout(ctx) }
    val NESTED_SCROLL_VIEW = { ctx: Context -> _NestedScrollView(ctx) }
}

inline fun ViewManager.viewPager(init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme = 0) { init() }
}

inline fun ViewManager.drawerLayout(init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.nestedScrollView(init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme = 0) { init() }
}

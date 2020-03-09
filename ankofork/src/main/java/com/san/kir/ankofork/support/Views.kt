package com.san.kir.ankofork.support

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.PagerTabStrip
import androidx.viewpager.widget.PagerTitleStrip
import androidx.viewpager.widget.ViewPager
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesSupportV4View {
    val PAGER_TAB_STRIP = { ctx: Context -> PagerTabStrip(ctx) }
    val PAGER_TITLE_STRIP = { ctx: Context -> PagerTitleStrip(ctx) }
    val CONTENT_LOADING_PROGRESS_BAR = { ctx: Context -> ContentLoadingProgressBar(ctx) }
}

inline fun ViewManager.pagerTabStrip(): PagerTabStrip = pagerTabStrip {}
inline fun ViewManager.pagerTabStrip(init: (@AnkoViewDslMarker PagerTabStrip).() -> Unit): PagerTabStrip {
    return ankoView(AnkoFactoriesSupportV4View.PAGER_TAB_STRIP, theme = 0) { init() }
}

inline fun ViewManager.themedPagerTabStrip(theme: Int = 0): PagerTabStrip = themedPagerTabStrip(theme) {}
inline fun ViewManager.themedPagerTabStrip(theme: Int = 0, init: (@AnkoViewDslMarker PagerTabStrip).() -> Unit): PagerTabStrip {
return ankoView(AnkoFactoriesSupportV4View.PAGER_TAB_STRIP, theme) { init() }
}

inline fun Context.pagerTabStrip(): PagerTabStrip = pagerTabStrip {}
inline fun Context.pagerTabStrip(init: (@AnkoViewDslMarker PagerTabStrip).() -> Unit): PagerTabStrip {
    return ankoView(AnkoFactoriesSupportV4View.PAGER_TAB_STRIP, theme = 0) { init() }
}

inline fun Context.themedPagerTabStrip(theme: Int = 0): PagerTabStrip = themedPagerTabStrip(theme) {}
inline fun Context.themedPagerTabStrip(theme: Int = 0, init: (@AnkoViewDslMarker PagerTabStrip).() -> Unit): PagerTabStrip {
return ankoView(AnkoFactoriesSupportV4View.PAGER_TAB_STRIP, theme) { init() }
}

inline fun Activity.pagerTabStrip(): PagerTabStrip = pagerTabStrip {}
inline fun Activity.pagerTabStrip(init: (@AnkoViewDslMarker PagerTabStrip).() -> Unit): PagerTabStrip {
    return ankoView(AnkoFactoriesSupportV4View.PAGER_TAB_STRIP, theme = 0) { init() }
}

inline fun Activity.themedPagerTabStrip(theme: Int = 0): PagerTabStrip = themedPagerTabStrip(theme) {}
inline fun Activity.themedPagerTabStrip(theme: Int = 0, init: (@AnkoViewDslMarker PagerTabStrip).() -> Unit): PagerTabStrip {
return ankoView(AnkoFactoriesSupportV4View.PAGER_TAB_STRIP, theme) { init() }
}

inline fun ViewManager.pagerTitleStrip(): PagerTitleStrip = pagerTitleStrip {}
inline fun ViewManager.pagerTitleStrip(init: (@AnkoViewDslMarker PagerTitleStrip).() -> Unit): PagerTitleStrip {
    return ankoView(AnkoFactoriesSupportV4View.PAGER_TITLE_STRIP, theme = 0) { init() }
}

inline fun ViewManager.themedPagerTitleStrip(theme: Int = 0): PagerTitleStrip = themedPagerTitleStrip(theme) {}
inline fun ViewManager.themedPagerTitleStrip(theme: Int = 0, init: (@AnkoViewDslMarker PagerTitleStrip).() -> Unit): PagerTitleStrip {
return ankoView(AnkoFactoriesSupportV4View.PAGER_TITLE_STRIP, theme) { init() }
}

inline fun Context.pagerTitleStrip(): PagerTitleStrip = pagerTitleStrip {}
inline fun Context.pagerTitleStrip(init: (@AnkoViewDslMarker PagerTitleStrip).() -> Unit): PagerTitleStrip {
    return ankoView(AnkoFactoriesSupportV4View.PAGER_TITLE_STRIP, theme = 0) { init() }
}

inline fun Context.themedPagerTitleStrip(theme: Int = 0): PagerTitleStrip = themedPagerTitleStrip(theme) {}
inline fun Context.themedPagerTitleStrip(theme: Int = 0, init: (@AnkoViewDslMarker PagerTitleStrip).() -> Unit): PagerTitleStrip {
return ankoView(AnkoFactoriesSupportV4View.PAGER_TITLE_STRIP, theme) { init() }
}

inline fun Activity.pagerTitleStrip(): PagerTitleStrip = pagerTitleStrip {}
inline fun Activity.pagerTitleStrip(init: (@AnkoViewDslMarker PagerTitleStrip).() -> Unit): PagerTitleStrip {
    return ankoView(AnkoFactoriesSupportV4View.PAGER_TITLE_STRIP, theme = 0) { init() }
}

inline fun Activity.themedPagerTitleStrip(theme: Int = 0): PagerTitleStrip = themedPagerTitleStrip(theme) {}
inline fun Activity.themedPagerTitleStrip(theme: Int = 0, init: (@AnkoViewDslMarker PagerTitleStrip).() -> Unit): PagerTitleStrip {
return ankoView(AnkoFactoriesSupportV4View.PAGER_TITLE_STRIP, theme) { init() }
}

inline fun ViewManager.contentLoadingProgressBar(): ContentLoadingProgressBar = contentLoadingProgressBar {}
inline fun ViewManager.contentLoadingProgressBar(init: (@AnkoViewDslMarker ContentLoadingProgressBar).() -> Unit): ContentLoadingProgressBar {
    return ankoView(AnkoFactoriesSupportV4View.CONTENT_LOADING_PROGRESS_BAR, theme = 0) { init() }
}

inline fun ViewManager.themedContentLoadingProgressBar(theme: Int = 0): ContentLoadingProgressBar = themedContentLoadingProgressBar(theme) {}
inline fun ViewManager.themedContentLoadingProgressBar(theme: Int = 0, init: (@AnkoViewDslMarker ContentLoadingProgressBar).() -> Unit): ContentLoadingProgressBar {
return ankoView(AnkoFactoriesSupportV4View.CONTENT_LOADING_PROGRESS_BAR, theme) { init() }
}


@PublishedApi
internal object AnkoFactoriesSupportV4ViewGroup {
    val VIEW_PAGER = { ctx: Context -> _ViewPager(ctx) }
    val DRAWER_LAYOUT = { ctx: Context -> _DrawerLayout(ctx) }
    val NESTED_SCROLL_VIEW = { ctx: Context -> _NestedScrollView(ctx) }
}

inline fun ViewManager.viewPager(): ViewPager = viewPager {}
inline fun ViewManager.viewPager(init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme = 0) { init() }
}

inline fun ViewManager.themedViewPager(theme: Int = 0): ViewPager = themedViewPager(theme) {}
inline fun ViewManager.themedViewPager(theme: Int = 0, init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme) { init() }
}

inline fun Context.viewPager(): ViewPager = viewPager {}
inline fun Context.viewPager(init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme = 0) { init() }
}

inline fun Context.themedViewPager(theme: Int = 0): ViewPager = themedViewPager(theme) {}
inline fun Context.themedViewPager(theme: Int = 0, init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme) { init() }
}

inline fun Activity.viewPager(): ViewPager = viewPager {}
inline fun Activity.viewPager(init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme = 0) { init() }
}

inline fun Activity.themedViewPager(theme: Int = 0): ViewPager = themedViewPager(theme) {}
inline fun Activity.themedViewPager(theme: Int = 0, init: (@AnkoViewDslMarker _ViewPager).() -> Unit): ViewPager {
return ankoView(AnkoFactoriesSupportV4ViewGroup.VIEW_PAGER, theme) { init() }
}

inline fun ViewManager.drawerLayout(): DrawerLayout = drawerLayout {}
inline fun ViewManager.drawerLayout(init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedDrawerLayout(theme: Int = 0): DrawerLayout = themedDrawerLayout(theme) {}
inline fun ViewManager.themedDrawerLayout(theme: Int = 0, init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme) { init() }
}

inline fun Context.drawerLayout(): DrawerLayout = drawerLayout {}
inline fun Context.drawerLayout(init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedDrawerLayout(theme: Int = 0): DrawerLayout = themedDrawerLayout(theme) {}
inline fun Context.themedDrawerLayout(theme: Int = 0, init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme) { init() }
}

inline fun Activity.drawerLayout(): DrawerLayout = drawerLayout {}
inline fun Activity.drawerLayout(init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedDrawerLayout(theme: Int = 0): DrawerLayout = themedDrawerLayout(theme) {}
inline fun Activity.themedDrawerLayout(theme: Int = 0, init: (@AnkoViewDslMarker _DrawerLayout).() -> Unit): DrawerLayout {
return ankoView(AnkoFactoriesSupportV4ViewGroup.DRAWER_LAYOUT, theme) { init() }
}

inline fun ViewManager.nestedScrollView(): NestedScrollView = nestedScrollView {}
inline fun ViewManager.nestedScrollView(init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedNestedScrollView(theme: Int = 0): NestedScrollView = themedNestedScrollView(theme) {}
inline fun ViewManager.themedNestedScrollView(theme: Int = 0, init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme) { init() }
}

inline fun Context.nestedScrollView(): NestedScrollView = nestedScrollView {}
inline fun Context.nestedScrollView(init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme = 0) { init() }
}

inline fun Context.themedNestedScrollView(theme: Int = 0): NestedScrollView = themedNestedScrollView(theme) {}
inline fun Context.themedNestedScrollView(theme: Int = 0, init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme) { init() }
}

inline fun Activity.nestedScrollView(): NestedScrollView = nestedScrollView {}
inline fun Activity.nestedScrollView(init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
    return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme = 0) { init() }
}

inline fun Activity.themedNestedScrollView(theme: Int = 0): NestedScrollView = themedNestedScrollView(theme) {}
inline fun Activity.themedNestedScrollView(theme: Int = 0, init: (@AnkoViewDslMarker _NestedScrollView).() -> Unit): NestedScrollView {
return ankoView(AnkoFactoriesSupportV4ViewGroup.NESTED_SCROLL_VIEW, theme) { init() }
}

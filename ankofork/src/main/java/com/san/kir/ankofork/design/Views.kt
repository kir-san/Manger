@file:JvmName("DesignViewsKt")
package com.san.kir.ankofork.design

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesDesignView {
    val FLOATING_ACTION_BUTTON = { ctx: Context -> FloatingActionButton(ctx) }
    val NAVIGATION_VIEW = { ctx: Context -> NavigationView(ctx) }
    val TAB_ITEM = { ctx: Context -> TabItem(ctx) }
    val TEXT_INPUT_EDIT_TEXT = { ctx: Context -> TextInputEditText(ctx) }
}

inline fun ViewManager.floatingActionButton(): FloatingActionButton = floatingActionButton {}
inline fun ViewManager.floatingActionButton(init: (@AnkoViewDslMarker FloatingActionButton).() -> Unit): FloatingActionButton {
    return ankoView(AnkoFactoriesDesignView.FLOATING_ACTION_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedFloatingActionButton(theme: Int = 0): FloatingActionButton = themedFloatingActionButton(theme) {}
inline fun ViewManager.themedFloatingActionButton(theme: Int = 0, init: (@AnkoViewDslMarker FloatingActionButton).() -> Unit): FloatingActionButton {
return ankoView(AnkoFactoriesDesignView.FLOATING_ACTION_BUTTON, theme) { init() }
}

inline fun ViewManager.navigationView(): NavigationView = navigationView {}
inline fun ViewManager.navigationView(init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
    return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedNavigationView(theme: Int = 0): NavigationView = themedNavigationView(theme) {}
inline fun ViewManager.themedNavigationView(theme: Int = 0, init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme) { init() }
}

inline fun Context.navigationView(): NavigationView = navigationView {}
inline fun Context.navigationView(init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
    return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun Context.themedNavigationView(theme: Int = 0): NavigationView = themedNavigationView(theme) {}
inline fun Context.themedNavigationView(theme: Int = 0, init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme) { init() }
}

inline fun Activity.navigationView(): NavigationView = navigationView {}
inline fun Activity.navigationView(init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
    return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun Activity.themedNavigationView(theme: Int = 0): NavigationView = themedNavigationView(theme) {}
inline fun Activity.themedNavigationView(theme: Int = 0, init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme) { init() }
}

inline fun ViewManager.tabItem(): TabItem = tabItem {}
inline fun ViewManager.tabItem(init: (@AnkoViewDslMarker TabItem).() -> Unit): TabItem {
    return ankoView(AnkoFactoriesDesignView.TAB_ITEM, theme = 0) { init() }
}

inline fun ViewManager.themedTabItem(theme: Int = 0): TabItem = themedTabItem(theme) {}
inline fun ViewManager.themedTabItem(theme: Int = 0, init: (@AnkoViewDslMarker TabItem).() -> Unit): TabItem {
return ankoView(AnkoFactoriesDesignView.TAB_ITEM, theme) { init() }
}

inline fun ViewManager.textInputEditText(): TextInputEditText = textInputEditText {}
inline fun ViewManager.textInputEditText(init: (@AnkoViewDslMarker TextInputEditText).() -> Unit): TextInputEditText {
    return ankoView(AnkoFactoriesDesignView.TEXT_INPUT_EDIT_TEXT, theme = 0) { init() }
}

inline fun ViewManager.themedTextInputEditText(theme: Int = 0): TextInputEditText = themedTextInputEditText(theme) {}
inline fun ViewManager.themedTextInputEditText(theme: Int = 0, init: (@AnkoViewDslMarker TextInputEditText).() -> Unit): TextInputEditText {
return ankoView(AnkoFactoriesDesignView.TEXT_INPUT_EDIT_TEXT, theme) { init() }
}

@PublishedApi
internal object AnkoFactoriesDesignViewGroup {
    val APP_BAR_LAYOUT = { ctx: Context -> _AppBarLayout(ctx) }
    val BOTTOM_NAVIGATION_VIEW = { ctx: Context -> _BottomNavigationView(ctx) }
    val COLLAPSING_TOOLBAR_LAYOUT = { ctx: Context -> _CollapsingToolbarLayout(ctx) }
    val COORDINATOR_LAYOUT = { ctx: Context -> _CoordinatorLayout(ctx) }
    val TAB_LAYOUT = { ctx: Context -> _TabLayout(ctx) }
    val TEXT_INPUT_LAYOUT = { ctx: Context -> _TextInputLayout(ctx) }
}

inline fun ViewManager.appBarLayout(): AppBarLayout = appBarLayout {}
inline fun ViewManager.appBarLayout(init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedAppBarLayout(theme: Int = 0): AppBarLayout = themedAppBarLayout(theme) {}
inline fun ViewManager.themedAppBarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme) { init() }
}

inline fun Context.appBarLayout(): AppBarLayout = appBarLayout {}
inline fun Context.appBarLayout(init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedAppBarLayout(theme: Int = 0): AppBarLayout = themedAppBarLayout(theme) {}
inline fun Context.themedAppBarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme) { init() }
}

inline fun Activity.appBarLayout(): AppBarLayout = appBarLayout {}
inline fun Activity.appBarLayout(init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedAppBarLayout(theme: Int = 0): AppBarLayout = themedAppBarLayout(theme) {}
inline fun Activity.themedAppBarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme) { init() }
}

inline fun ViewManager.bottomNavigationView(): BottomNavigationView = bottomNavigationView {}
inline fun ViewManager.bottomNavigationView(init: (@AnkoViewDslMarker _BottomNavigationView).() -> Unit): BottomNavigationView {
    return ankoView(AnkoFactoriesDesignViewGroup.BOTTOM_NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedBottomNavigationView(theme: Int = 0): BottomNavigationView = themedBottomNavigationView(theme) {}
inline fun ViewManager.themedBottomNavigationView(theme: Int = 0, init: (@AnkoViewDslMarker _BottomNavigationView).() -> Unit): BottomNavigationView {
return ankoView(AnkoFactoriesDesignViewGroup.BOTTOM_NAVIGATION_VIEW, theme) { init() }
}

inline fun Context.bottomNavigationView(): BottomNavigationView = bottomNavigationView {}
inline fun Context.bottomNavigationView(init: (@AnkoViewDslMarker _BottomNavigationView).() -> Unit): BottomNavigationView {
    return ankoView(AnkoFactoriesDesignViewGroup.BOTTOM_NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun Context.themedBottomNavigationView(theme: Int = 0): BottomNavigationView = themedBottomNavigationView(theme) {}
inline fun Context.themedBottomNavigationView(theme: Int = 0, init: (@AnkoViewDslMarker _BottomNavigationView).() -> Unit): BottomNavigationView {
return ankoView(AnkoFactoriesDesignViewGroup.BOTTOM_NAVIGATION_VIEW, theme) { init() }
}

inline fun Activity.bottomNavigationView(): BottomNavigationView = bottomNavigationView {}
inline fun Activity.bottomNavigationView(init: (@AnkoViewDslMarker _BottomNavigationView).() -> Unit): BottomNavigationView {
    return ankoView(AnkoFactoriesDesignViewGroup.BOTTOM_NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun Activity.themedBottomNavigationView(theme: Int = 0): BottomNavigationView = themedBottomNavigationView(theme) {}
inline fun Activity.themedBottomNavigationView(theme: Int = 0, init: (@AnkoViewDslMarker _BottomNavigationView).() -> Unit): BottomNavigationView {
return ankoView(AnkoFactoriesDesignViewGroup.BOTTOM_NAVIGATION_VIEW, theme) { init() }
}

inline fun ViewManager.collapsingToolbarLayout(): CollapsingToolbarLayout = collapsingToolbarLayout {}
inline fun ViewManager.collapsingToolbarLayout(init: (@AnkoViewDslMarker _CollapsingToolbarLayout).() -> Unit): CollapsingToolbarLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COLLAPSING_TOOLBAR_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedCollapsingToolbarLayout(theme: Int = 0): CollapsingToolbarLayout = themedCollapsingToolbarLayout(theme) {}
inline fun ViewManager.themedCollapsingToolbarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _CollapsingToolbarLayout).() -> Unit): CollapsingToolbarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.COLLAPSING_TOOLBAR_LAYOUT, theme) { init() }
}

inline fun Context.collapsingToolbarLayout(): CollapsingToolbarLayout = collapsingToolbarLayout {}
inline fun Context.collapsingToolbarLayout(init: (@AnkoViewDslMarker _CollapsingToolbarLayout).() -> Unit): CollapsingToolbarLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COLLAPSING_TOOLBAR_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedCollapsingToolbarLayout(theme: Int = 0): CollapsingToolbarLayout = themedCollapsingToolbarLayout(theme) {}
inline fun Context.themedCollapsingToolbarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _CollapsingToolbarLayout).() -> Unit): CollapsingToolbarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.COLLAPSING_TOOLBAR_LAYOUT, theme) { init() }
}

inline fun Activity.collapsingToolbarLayout(): CollapsingToolbarLayout = collapsingToolbarLayout {}
inline fun Activity.collapsingToolbarLayout(init: (@AnkoViewDslMarker _CollapsingToolbarLayout).() -> Unit): CollapsingToolbarLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COLLAPSING_TOOLBAR_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedCollapsingToolbarLayout(theme: Int = 0): CollapsingToolbarLayout = themedCollapsingToolbarLayout(theme) {}
inline fun Activity.themedCollapsingToolbarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _CollapsingToolbarLayout).() -> Unit): CollapsingToolbarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.COLLAPSING_TOOLBAR_LAYOUT, theme) { init() }
}

inline fun ViewManager.coordinatorLayout(): CoordinatorLayout = coordinatorLayout {}
inline fun ViewManager.coordinatorLayout(init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedCoordinatorLayout(theme: Int = 0): CoordinatorLayout = themedCoordinatorLayout(theme) {}
inline fun ViewManager.themedCoordinatorLayout(theme: Int = 0, init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme) { init() }
}

inline fun Context.coordinatorLayout(): CoordinatorLayout = coordinatorLayout {}
inline fun Context.coordinatorLayout(init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedCoordinatorLayout(theme: Int = 0): CoordinatorLayout = themedCoordinatorLayout(theme) {}
inline fun Context.themedCoordinatorLayout(theme: Int = 0, init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme) { init() }
}

inline fun Activity.coordinatorLayout(): CoordinatorLayout = coordinatorLayout {}
inline fun Activity.coordinatorLayout(init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedCoordinatorLayout(theme: Int = 0): CoordinatorLayout = themedCoordinatorLayout(theme) {}
inline fun Activity.themedCoordinatorLayout(theme: Int = 0, init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme) { init() }
}

inline fun ViewManager.tabLayout(): TabLayout = tabLayout {}
inline fun ViewManager.tabLayout(init: (@AnkoViewDslMarker _TabLayout).() -> Unit): TabLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TAB_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedTabLayout(theme: Int = 0): TabLayout = themedTabLayout(theme) {}
inline fun ViewManager.themedTabLayout(theme: Int = 0, init: (@AnkoViewDslMarker _TabLayout).() -> Unit): TabLayout {
return ankoView(AnkoFactoriesDesignViewGroup.TAB_LAYOUT, theme) { init() }
}

inline fun Context.tabLayout(): TabLayout = tabLayout {}
inline fun Context.tabLayout(init: (@AnkoViewDslMarker _TabLayout).() -> Unit): TabLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TAB_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedTabLayout(theme: Int = 0): TabLayout = themedTabLayout(theme) {}
inline fun Context.themedTabLayout(theme: Int = 0, init: (@AnkoViewDslMarker _TabLayout).() -> Unit): TabLayout {
return ankoView(AnkoFactoriesDesignViewGroup.TAB_LAYOUT, theme) { init() }
}

inline fun Activity.tabLayout(): TabLayout = tabLayout {}
inline fun Activity.tabLayout(init: (@AnkoViewDslMarker _TabLayout).() -> Unit): TabLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TAB_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedTabLayout(theme: Int = 0): TabLayout = themedTabLayout(theme) {}
inline fun Activity.themedTabLayout(theme: Int = 0, init: (@AnkoViewDslMarker _TabLayout).() -> Unit): TabLayout {
return ankoView(AnkoFactoriesDesignViewGroup.TAB_LAYOUT, theme) { init() }
}

inline fun ViewManager.textInputLayout(): TextInputLayout = textInputLayout {}
inline fun ViewManager.textInputLayout(init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedTextInputLayout(theme: Int = 0): TextInputLayout = themedTextInputLayout(theme) {}
inline fun ViewManager.themedTextInputLayout(theme: Int = 0, init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme) { init() }
}

inline fun Context.textInputLayout(): TextInputLayout = textInputLayout {}
inline fun Context.textInputLayout(init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedTextInputLayout(theme: Int = 0): TextInputLayout = themedTextInputLayout(theme) {}
inline fun Context.themedTextInputLayout(theme: Int = 0, init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme) { init() }
}

inline fun Activity.textInputLayout(): TextInputLayout = textInputLayout {}
inline fun Activity.textInputLayout(init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedTextInputLayout(theme: Int = 0): TextInputLayout = themedTextInputLayout(theme) {}
inline fun Activity.themedTextInputLayout(theme: Int = 0, init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme) { init() }
}


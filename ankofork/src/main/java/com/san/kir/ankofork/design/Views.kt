@file:JvmName("DesignViewsKt")
package com.san.kir.ankofork.design

import android.content.Context
import android.view.ViewManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesDesignView {
    val FLOATING_ACTION_BUTTON = { ctx: Context -> FloatingActionButton(ctx) }
    val NAVIGATION_VIEW = { ctx: Context -> NavigationView(ctx) }
    val TEXT_INPUT_EDIT_TEXT = { ctx: Context -> TextInputEditText(ctx) }
}

inline fun ViewManager.floatingActionButton(init: (@AnkoViewDslMarker FloatingActionButton).() -> Unit): FloatingActionButton {
    return ankoView(AnkoFactoriesDesignView.FLOATING_ACTION_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.navigationView(init: (@AnkoViewDslMarker NavigationView).() -> Unit): NavigationView {
    return ankoView(AnkoFactoriesDesignView.NAVIGATION_VIEW, theme = 0) { init() }
}

inline fun ViewManager.textInputEditText(init: (@AnkoViewDslMarker TextInputEditText).() -> Unit): TextInputEditText {
    return ankoView(AnkoFactoriesDesignView.TEXT_INPUT_EDIT_TEXT, theme = 0) { init() }
}

@PublishedApi
internal object AnkoFactoriesDesignViewGroup {
    val APP_BAR_LAYOUT = { ctx: Context -> _AppBarLayout(ctx) }
    val COORDINATOR_LAYOUT = { ctx: Context -> _CoordinatorLayout(ctx) }
    val TEXT_INPUT_LAYOUT = { ctx: Context -> _TextInputLayout(ctx) }
}

inline fun ViewManager.themedAppBarLayout(theme: Int = 0, init: (@AnkoViewDslMarker _AppBarLayout).() -> Unit): AppBarLayout {
return ankoView(AnkoFactoriesDesignViewGroup.APP_BAR_LAYOUT, theme) { init() }
}

inline fun ViewManager.coordinatorLayout(init: (@AnkoViewDslMarker _CoordinatorLayout).() -> Unit): CoordinatorLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.COORDINATOR_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.textInputLayout(init: (@AnkoViewDslMarker _TextInputLayout).() -> Unit): TextInputLayout {
    return ankoView(AnkoFactoriesDesignViewGroup.TEXT_INPUT_LAYOUT, theme = 0) { init() }
}

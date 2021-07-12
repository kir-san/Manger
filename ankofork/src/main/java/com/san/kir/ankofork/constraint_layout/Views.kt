package com.san.kir.ankofork.constraint_layout

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesConstraintLayoutView {
    val GROUP = { ctx: Context -> Group(ctx) }
}

inline fun ViewManager.group(): Group = group {}
inline fun ViewManager.group(init: (@AnkoViewDslMarker Group).() -> Unit): Group {
    return ankoView(AnkoFactoriesConstraintLayoutView.GROUP, theme = 0) { init() }
}

@PublishedApi
internal object AnkoFactoriesConstraintLayoutViewGroup {
    val CONSTRAINT_LAYOUT = { ctx: Context -> _ConstraintLayout(ctx) }
}

inline fun ViewManager.constraintLayout(init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
    return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme = 0) { init() }
}

inline fun Activity.constraintLayout(init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
    return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme = 0) { init() }
}


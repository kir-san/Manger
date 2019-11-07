package com.san.kir.ankofork.constraint_layout

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.constraintlayout.widget.Guideline
import androidx.constraintlayout.widget.Placeholder
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesConstraintLayoutView {
    val BARRIER = { ctx: Context -> Barrier(ctx) }
    val GROUP = { ctx: Context -> Group(ctx) }
    val GUIDELINE = { ctx: Context -> Guideline(ctx) }
    val PLACEHOLDER = { ctx: Context -> Placeholder(ctx) }
}

inline fun ViewManager.barrier(): Barrier = barrier {}
inline fun ViewManager.barrier(init: (@AnkoViewDslMarker Barrier).() -> Unit): Barrier {
    return ankoView(AnkoFactoriesConstraintLayoutView.BARRIER, theme = 0) { init() }
}

inline fun ViewManager.themedBarrier(theme: Int = 0): Barrier = themedBarrier(theme) {}
inline fun ViewManager.themedBarrier(theme: Int = 0, init: (@AnkoViewDslMarker Barrier).() -> Unit): Barrier {
return ankoView(AnkoFactoriesConstraintLayoutView.BARRIER, theme) { init() }
}

inline fun ViewManager.group(): Group = group {}
inline fun ViewManager.group(init: (@AnkoViewDslMarker Group).() -> Unit): Group {
    return ankoView(AnkoFactoriesConstraintLayoutView.GROUP, theme = 0) { init() }
}

inline fun ViewManager.themedGroup(theme: Int = 0): Group = themedGroup(theme) {}
inline fun ViewManager.themedGroup(theme: Int = 0, init: (@AnkoViewDslMarker Group).() -> Unit): Group {
return ankoView(AnkoFactoriesConstraintLayoutView.GROUP, theme) { init() }
}

inline fun ViewManager.guideline(): Guideline = guideline {}
inline fun ViewManager.guideline(init: (@AnkoViewDslMarker Guideline).() -> Unit): Guideline {
    return ankoView(AnkoFactoriesConstraintLayoutView.GUIDELINE, theme = 0) { init() }
}

inline fun ViewManager.themedGuideline(theme: Int = 0): Guideline = themedGuideline(theme) {}
inline fun ViewManager.themedGuideline(theme: Int = 0, init: (@AnkoViewDslMarker Guideline).() -> Unit): Guideline {
return ankoView(AnkoFactoriesConstraintLayoutView.GUIDELINE, theme) { init() }
}

inline fun ViewManager.placeholder(): Placeholder = placeholder {}
inline fun ViewManager.placeholder(init: (@AnkoViewDslMarker Placeholder).() -> Unit): Placeholder {
    return ankoView(AnkoFactoriesConstraintLayoutView.PLACEHOLDER, theme = 0) { init() }
}

inline fun ViewManager.themedPlaceholder(theme: Int = 0): Placeholder = themedPlaceholder(theme) {}
inline fun ViewManager.themedPlaceholder(theme: Int = 0, init: (@AnkoViewDslMarker Placeholder).() -> Unit): Placeholder {
return ankoView(AnkoFactoriesConstraintLayoutView.PLACEHOLDER, theme) { init() }
}

@PublishedApi
internal object AnkoFactoriesConstraintLayoutViewGroup {
    val CONSTRAINT_LAYOUT = { ctx: Context -> _ConstraintLayout(ctx) }
}

inline fun ViewManager.constraintLayout(): ConstraintLayout = constraintLayout {}
inline fun ViewManager.constraintLayout(init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
    return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedConstraintLayout(theme: Int = 0): ConstraintLayout = themedConstraintLayout(theme) {}
inline fun ViewManager.themedConstraintLayout(theme: Int = 0, init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme) { init() }
}

inline fun Context.constraintLayout(): ConstraintLayout = constraintLayout {}
inline fun Context.constraintLayout(init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
    return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedConstraintLayout(theme: Int = 0): ConstraintLayout = themedConstraintLayout(theme) {}
inline fun Context.themedConstraintLayout(theme: Int = 0, init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme) { init() }
}

inline fun Activity.constraintLayout(): ConstraintLayout = constraintLayout {}
inline fun Activity.constraintLayout(init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
    return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedConstraintLayout(theme: Int = 0): ConstraintLayout = themedConstraintLayout(theme) {}
inline fun Activity.themedConstraintLayout(theme: Int = 0, init: (@AnkoViewDslMarker _ConstraintLayout).() -> Unit): ConstraintLayout {
return ankoView(AnkoFactoriesConstraintLayoutViewGroup.CONSTRAINT_LAYOUT, theme) { init() }
}


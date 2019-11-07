package com.san.kir.ankofork

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import androidx.fragment.app.Fragment
import com.san.kir.ankofork.AnkoInternals.createAnkoContext

@DslMarker
private annotation class AnkoContextDslMarker

@AnkoContextDslMarker
interface AnkoContext<out T> : ViewManager {
    val ctx: Context
    val owner: T
    val view: View

    override fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
        throw UnsupportedOperationException()
    }

    override fun removeView(view: View) {
        throw UnsupportedOperationException()
    }

    companion object {
        fun create(ctx: Context, setContentView: Boolean = false): AnkoContext<Context>
                = AnkoContextImpl(ctx, ctx, setContentView)

        fun createReusable(ctx: Context, setContentView: Boolean = false): AnkoContext<Context>
                = ReusableAnkoContext(ctx, ctx, setContentView)

        fun <T> create(ctx: Context, owner: T, setContentView: Boolean = false): AnkoContext<T>
                = AnkoContextImpl(ctx, owner, setContentView)

        fun <T> createReusable(ctx: Context, owner: T, setContentView: Boolean = false): AnkoContext<T>
                = ReusableAnkoContext(ctx, owner, setContentView)

        fun <T: ViewGroup> createDelegate(owner: T): AnkoContext<T> = DelegatingAnkoContext(owner)
    }
}

internal class DelegatingAnkoContext<T: ViewGroup>(override val owner: T): AnkoContext<T> {
    override val ctx: Context = owner.context
    override val view: View = owner

    override fun addView(view: View?, params: ViewGroup.LayoutParams?) {
        if (view == null) return

        if (params == null) {
            owner.addView(view)
        } else {
            owner.addView(view, params)
        }
    }
}

internal class ReusableAnkoContext<T>(
        override val ctx: Context,
        override val owner: T,
        setContentView: Boolean
) : AnkoContextImpl<T>(ctx, owner, setContentView) {
    override fun alreadyHasView() {}
}

open class AnkoContextImpl<T>(
        override val ctx: Context,
        override val owner: T,
        private val setContentView: Boolean
) : AnkoContext<T> {
    private var myView: View? = null

    override val view: View
        get() = myView ?: throw IllegalStateException("View was not set previously")

    override fun addView(view: View?, params: ViewGroup.LayoutParams?) {
        if (view == null) return

        if (myView != null) {
            alreadyHasView()
        }

        this.myView = view

        if (setContentView) {
            doAddView(ctx, view)
        }
    }

    private fun doAddView(context: Context, view: View) {
        when (context) {
            is Activity -> context.setContentView(view)
            is ContextWrapper -> doAddView(context.baseContext, view)
            else -> throw IllegalStateException("Context is not an Activity, can't set content view")
        }
    }

    protected open fun alreadyHasView(): Unit = throw IllegalStateException("View is already set: $myView")
}

inline fun Context.ui(setContentView: Boolean, init: AnkoContext<Context>.() -> Unit): AnkoContext<Context> =
        createAnkoContext(this, init, setContentView)

inline fun Context.ui(init: AnkoContext<Context>.() -> Unit): AnkoContext<Context> =
        createAnkoContext(this, init)

inline fun Fragment.ui(init: AnkoContext<Fragment>.() -> Unit): AnkoContext<Fragment> =
        createAnkoContext(requireActivity(), init)

interface AnkoComponent<in T> {
    fun createView(ui: AnkoContext<T>): View
}

fun <T : Activity> AnkoComponent<T>.setContentView(activity: T): View =
        createView(AnkoContextImpl(activity, activity, true))

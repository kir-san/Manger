package com.san.kir.manger.Extending.AnkoExtend

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.san.kir.manger.Extending.Views.ExpandableFrameLayout
import com.san.kir.manger.Extending.Views.SquareRelativeLayout

private val defaultInit: Any.() -> Unit = {}

open class _SquareRelativeLayout(ctx: Context): SquareRelativeLayout(ctx) {
    fun <T: View> T.lparams(
            c: Context?,
            attrs: AttributeSet?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(c!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            source: ViewGroup.LayoutParams?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            source: ViewGroup.MarginLayoutParams?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            source: RelativeLayout.LayoutParams?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

}

open class _ExpandableFrameLayout(context: Context) : ExpandableFrameLayout(context) {
    fun <T: View> T.lparams(
            c: Context?,
            attrs: AttributeSet?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(c!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            source: ViewGroup.LayoutParams?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            source: ViewGroup.MarginLayoutParams?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T: View> T.lparams(
            source: RelativeLayout.LayoutParams?,
            init: RelativeLayout.LayoutParams.() -> Unit = defaultInit
    ): T {
        val layoutParams = RelativeLayout.LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

}

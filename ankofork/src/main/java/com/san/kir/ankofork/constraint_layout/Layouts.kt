package com.san.kir.ankofork.constraint_layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

@Suppress("ClassName")
open class _ConstraintLayout(ctx: Context): ConstraintLayout(ctx) {

    inline fun <T: View> T.lparams(
            source: LayoutParams?,
            init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        layoutParams.validate()
        this@lparams.layoutParams = layoutParams
        return this
    }

     fun <T: View> T.lparams(
            source: LayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T: View> T.lparams(
            c: Context?,
            attrs: AttributeSet?,
            init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(c!!, attrs!!)
        layoutParams.init()
        layoutParams.validate()
        this@lparams.layoutParams = layoutParams
        return this
    }

     fun <T: View> T.lparams(
            c: Context?,
            attrs: AttributeSet?
    ): T {
        val layoutParams = LayoutParams(c!!, attrs!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T: View> T.lparams(
            width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
            init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(width, height)
        layoutParams.init()
        layoutParams.validate()
        this@lparams.layoutParams = layoutParams
        return this
    }

     fun <T: View> T.lparams(
            width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ): T {
        val layoutParams = LayoutParams(width, height)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T: View> T.lparams(
            source: ViewGroup.LayoutParams?,
            init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        layoutParams.validate()
        this@lparams.layoutParams = layoutParams
        return this
    }

     fun <T: View> T.lparams(
            source: ViewGroup.LayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

}


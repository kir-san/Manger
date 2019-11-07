@file:JvmName("SupportV4LayoutsKt")

package com.san.kir.ankofork.support

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager

@Suppress("ClassName")
open class _ViewPager(ctx: Context) : ViewPager(ctx) {

    inline fun <T : View> T.lparams(
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams()
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(

    ): T {
        val layoutParams = LayoutParams()
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        context: Context?,
        attrs: AttributeSet?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(context!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        context: Context?,
        attrs: AttributeSet?
    ): T {
        val layoutParams = LayoutParams(context!!, attrs!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

}

@Suppress("ClassName")
open class _DrawerLayout(ctx: Context) : DrawerLayout(ctx) {

    inline fun <T : View> T.lparams(
        c: Context?,
        attrs: AttributeSet?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(c!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        c: Context?,
        attrs: AttributeSet?
    ): T {
        val layoutParams = LayoutParams(c!!, attrs!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ): T {
        val layoutParams = LayoutParams(width, height)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        gravity: Int,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(width, height, gravity)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        gravity: Int
    ): T {
        val layoutParams = LayoutParams(width, height, gravity)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        source: LayoutParams?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        source: LayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        source: ViewGroup.LayoutParams?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        source: ViewGroup.LayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        source: MarginLayoutParams?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        source: MarginLayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

}

@Suppress("ClassName")
open class _NestedScrollView(ctx: Context) : NestedScrollView(ctx) {

    inline fun <T : View> T.lparams(
        c: Context?,
        attrs: AttributeSet?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(c!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        c: Context?,
        attrs: AttributeSet?
    ): T {
        val layoutParams = LayoutParams(c!!, attrs!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ): T {
        val layoutParams = LayoutParams(width, height)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        gravity: Int,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(width, height, gravity)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        gravity: Int
    ): T {
        val layoutParams = LayoutParams(width, height, gravity)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        source: ViewGroup.LayoutParams?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        source: ViewGroup.LayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        source: MarginLayoutParams?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        source: MarginLayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(
        source: LayoutParams?,
        init: LayoutParams.() -> Unit
    ): T {
        val layoutParams = LayoutParams(source!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(
        source: LayoutParams?
    ): T {
        val layoutParams = LayoutParams(source!!)
        this@lparams.layoutParams = layoutParams
        return this
    }

}

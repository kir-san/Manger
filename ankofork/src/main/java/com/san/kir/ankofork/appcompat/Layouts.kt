package com.san.kir.ankofork.appcompat


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar

@Suppress("ClassName")
open class _Toolbar(ctx: Context) : Toolbar(ctx) {

    fun <T : View> T.lparams(
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ): T {
        val layoutParams = LayoutParams(width, height)
        this@lparams.layoutParams = layoutParams
        return this
    }

}


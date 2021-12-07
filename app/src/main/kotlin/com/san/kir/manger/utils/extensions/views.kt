package com.san.kir.manger.utils.extensions

import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.san.kir.ankofork.doFromSdk

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.doOnApplyWindowInstets(block: (View, WindowInsetsCompat, Rect) -> WindowInsetsCompat) {
    val initialPadding = recordInitialPaddingForView(this)

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        block(v, insets, initialPadding)
    }
    doFromSdk(20) {
        requestApplyInsetsWhenAttached()
    }
}

@RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

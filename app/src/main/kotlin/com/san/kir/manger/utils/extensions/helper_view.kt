package com.san.kir.manger.utils.extensions

import android.graphics.Rect
import android.view.View

fun recordInitialPaddingForView(view: View) =
    Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)


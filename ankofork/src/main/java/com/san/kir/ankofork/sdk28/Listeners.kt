package com.san.kir.ankofork.sdk28

import android.view.View


inline fun View.onClick(noinline l: (v: View?) -> Unit) {
    setOnClickListener(l)
}


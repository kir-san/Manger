package com.san.kir.manger.photoview

import com.san.kir.manger.picasso.Callback

fun onError(action: () -> Unit): Callback {
    return object : Callback {
        override fun onSuccess() {

        }

        override fun onError(e: java.lang.Exception?) {
            action()
        }
    }
}

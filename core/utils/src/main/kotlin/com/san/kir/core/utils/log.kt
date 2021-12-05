package com.san.kir.core.utils

import android.util.Log

const val TAG = "myLogs"

var log: String = ""
    set(msg) {
        Log.v(TAG, msg)
    }

fun Any.log(msg: String) {
    Log.v(this::class.java.simpleName, msg)
}

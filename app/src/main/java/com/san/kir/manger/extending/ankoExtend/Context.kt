package com.san.kir.manger.extending.ankoExtend

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.os.Build
import org.jetbrains.anko.internals.AnkoInternals


inline fun <reified T : Service> Context.startForegroundService(vararg params: Pair<String, Any?>): ComponentName? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(AnkoInternals.createIntent(this, T::class.java, params))
    } else {
        startService(AnkoInternals.createIntent(this, T::class.java, params))
    }
}

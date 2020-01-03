package com.san.kir.manger.utils.extensions

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.san.kir.ankofork.AnkoInternals
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.sdk28.connectivityManager


inline fun <reified T : Service> Context.startForegroundService(vararg params: Pair<String, Any?>): ComponentName? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(AnkoInternals.createIntent(this, T::class.java, params))
    } else {
        startService(AnkoInternals.createIntent(this, T::class.java, params))
    }
}

fun Context.startForegroundServiceIntent(intent: Intent): ComponentName? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Context.isOnWifi(): Boolean {
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    } else {
        false
    }
}

fun Context.isNetworkAvailable(): Boolean {
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun Context.getDrawableCompat(layoutRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, layoutRes)
}

fun Context.quantitySimple(@PluralsRes id: Int, quantity: Int): String {
    return resources.getQuantityString(id, quantity, quantity)
}

fun Context.longToast(@StringRes resId:Int, vararg formatArgs: Any?) {
    longToast(getString(resId, *formatArgs))
}

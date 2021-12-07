package com.san.kir.manger.utils.extensions

import android.app.Service
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.sdk28.connectivityManager
import java.io.Serializable

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

fun Context.quantitySimple(@PluralsRes id: Int, quantity: Int): String {
    return resources.getQuantityString(id, quantity, quantity)
}

fun Context.longToast(@StringRes resId: Int, vararg formatArgs: Any?) {
    longToast(getString(resId, *formatArgs))
}

inline fun <reified T : Service> startService(
    ctx: Context,
    action: String,
    vararg params: Pair<String, Any?>,
): ComponentName? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ctx.startForegroundService(intentFor<T>(ctx, *params).setAction(action))
    } else {
        ctx.startService(intentFor<T>(ctx, *params).setAction(action))
    }
}

inline fun <reified T : Service> startService(ctx: Context, action: String): ComponentName? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ctx.startForegroundService(intentFor<T>(ctx).setAction(action))
    } else {
        ctx.startService(intentFor<T>(ctx).setAction(action))
    }
}

inline fun <reified T : Service> startService(
    ctx: Context,
    vararg params: Pair<String, Any?>,
): ComponentName? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ctx.startForegroundService(intentFor<T>(ctx, *params))
    } else {
        ctx.startService(intentFor<T>(ctx, *params))
    }
}

inline fun <reified T : Any> intentFor(ctx: Context, vararg params: Pair<String, Any?>): Intent {
    val intent = Intent(ctx, T::class.java)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    return intent
}


fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
    params.forEach {
        intent.addArgument(it)
        return@forEach
    }
}

fun Intent.addArgument(param: Pair<String, Any?>) {
    when (val value = param.second) {
        null -> putExtra(param.first, null as Serializable?)
        is Int -> putExtra(param.first, value)
        is Long -> putExtra(param.first, value)
        is CharSequence -> putExtra(param.first, value)
        is String -> putExtra(param.first, value)
        is Float -> putExtra(param.first, value)
        is Double -> putExtra(param.first, value)
        is Char -> putExtra(param.first, value)
        is Short -> putExtra(param.first, value)
        is Boolean -> putExtra(param.first, value)
        is Serializable -> putExtra(param.first, value)
        is Bundle -> putExtra(param.first, value)
        is Parcelable -> putExtra(param.first, value)
        is Array<*> -> when {
            value.isArrayOf<CharSequence>() -> putExtra(param.first, value)
            value.isArrayOf<String>() -> putExtra(param.first, value)
            value.isArrayOf<Parcelable>() -> putExtra(param.first, value)
            else -> throw Exception("Intent extra ${param.first} has wrong type ${value.javaClass.name}")
        }
        is IntArray -> putExtra(param.first, value)
        is LongArray -> putExtra(param.first, value)
        is FloatArray -> putExtra(param.first, value)
        is DoubleArray -> putExtra(param.first, value)
        is CharArray -> putExtra(param.first, value)
        is ShortArray -> putExtra(param.first, value)
        is BooleanArray -> putExtra(param.first, value)
        else -> throw Exception("Intent extra ${param.first} has wrong type ${value.javaClass.name}")
    }
}

fun Context.browse(url: String, newTask: Boolean = false): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

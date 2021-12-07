package com.san.kir.manger.utils

import android.app.Application
import com.san.kir.manger.utils.extensions.isNetworkAvailable
import com.san.kir.manger.utils.extensions.isOnWifi
import javax.inject.Inject

class NetworkManager @Inject constructor(private val context: Application) {

    @Volatile
    var isWifi = false

    fun isAvailable() = if (isWifi) context.isOnWifi() else context.isNetworkAvailable()
}

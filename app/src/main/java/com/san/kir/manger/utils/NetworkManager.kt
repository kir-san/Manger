package com.san.kir.manger.utils

import android.content.Context
import com.san.kir.manger.utils.extensions.isNetworkAvailable
import com.san.kir.manger.utils.extensions.isOnWifi

class NetworkManager(private val context: Context) {

    @Volatile
    var isWifi = false

    fun isAvailable() = if (isWifi) context.isOnWifi() else context.isNetworkAvailable()
}

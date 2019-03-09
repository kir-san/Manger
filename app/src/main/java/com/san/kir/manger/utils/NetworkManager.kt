package com.san.kir.manger.utils

import android.content.Context
import com.san.kir.manger.extending.anko_extend.isNetworkAvailable
import com.san.kir.manger.extending.anko_extend.isOnWifi

class NetworkManager(private val context: Context) {

    @Volatile
    var isWifi = false

    fun isAvailable() = if (isWifi) context.isOnWifi() else context.isNetworkAvailable()
}

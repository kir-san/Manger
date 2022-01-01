package com.san.kir.core.download

import android.app.Application
import com.san.kir.core.utils.isNetworkAvailable
import com.san.kir.core.utils.isOnWifi
import javax.inject.Inject

class NetworkManager @Inject constructor(private val context: Application) {

    @Volatile
    var isWifi = false

    fun isAvailable() = if (isWifi) context.isOnWifi() else context.isNetworkAvailable()
}

package com.san.kir.ankofork.sdk28


import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager


/** Returns the ConnectivityManager instance. **/
val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


/** Returns the NotificationManager instance. **/
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

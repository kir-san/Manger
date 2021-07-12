package com.san.kir.ankofork.sdk28


import android.app.NotificationManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi


/** Returns the ConnectivityManager instance. **/
val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


/** Returns the NotificationManager instance. **/
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


/** Returns the StorageManager instance. **/
@Suppress("unused")
val Context.storageManager: StorageManager
    get() = getSystemService(Context.STORAGE_SERVICE) as StorageManager


/** Returns the StorageStatsManager instance. **/
@Suppress("unused")
val Context.storageStatsManager: StorageStatsManager
    @RequiresApi(Build.VERSION_CODES.O)
    get() = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager



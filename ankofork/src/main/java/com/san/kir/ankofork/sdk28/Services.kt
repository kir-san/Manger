package com.san.kir.ankofork.sdk28


import android.accounts.AccountManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.SearchManager
import android.app.UiModeManager
import android.app.WallpaperManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ShortcutManager
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.UserManager
import android.os.storage.StorageManager
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi


/** Returns the AccountManager instance. **/
val Context.accountManager: AccountManager
    get() = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager


/** Returns the ConnectivityManager instance. **/
val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


/** Returns the InputMethodManager instance. **/
val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


/** Returns the KeyguardManager instance. **/
val Context.keyguardManager: KeyguardManager
    get() = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager


/** Returns the NotificationManager instance. **/
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


/** Returns the SearchManager instance. **/
val Context.searchManager: SearchManager
    get() = getSystemService(Context.SEARCH_SERVICE) as SearchManager


/** Returns the SensorManager instance. **/
val Context.sensorManager: SensorManager
    get() = getSystemService(Context.SENSOR_SERVICE) as SensorManager


/** Returns the ShortcutManager instance. **/
val Context.shortcutManager: ShortcutManager
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    get() = getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager


/** Returns the StorageManager instance. **/
val Context.storageManager: StorageManager
    get() = getSystemService(Context.STORAGE_SERVICE) as StorageManager


/** Returns the StorageStatsManager instance. **/
val Context.storageStatsManager: StorageStatsManager
    @RequiresApi(Build.VERSION_CODES.O)
    get() = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager


/** Returns the UiModeManager instance. **/
val Context.uiModeManager: UiModeManager
    get() = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager


/** Returns the UserManager instance. **/
val Context.userManager: UserManager
    get() = getSystemService(Context.USER_SERVICE) as UserManager


/** Returns the WallpaperManager instance. **/
val Context.wallpaperManager: WallpaperManager
    get() = getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager


/** Returns the WifiManager instance. **/
val Context.wifiManager: WifiManager
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


/** Returns the WifiP2pManager instance. **/
val Context.wifiP2pManager: WifiP2pManager
    get() = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager


/** Returns the WindowManager instance. **/
val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager


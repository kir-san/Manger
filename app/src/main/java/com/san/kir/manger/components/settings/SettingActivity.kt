package com.san.kir.manger.components.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.downloadManager.ChapterLoaderC
import com.san.kir.manger.components.downloadManager.DownloadService
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.frameLayout

class SettingActivity : DrawerActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val content = ID.generate()

    var downloadManager: ChapterLoaderC? = null
    private var bound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadManager =
                    (service as DownloadService.LocalBinderC).chapterLoader
            bound = true
        }
    }

    override val LinearLayout.customView: View
        get() = frameLayout { id = content }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.main_menu_settings)

        supportFragmentManager.beginTransaction()
                .replace(content, PrefFragment())
                .commit()

        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.settings_downloader_parallel_key) -> {
                val default = getString(R.string.settings_downloader_parallel_default) == "true"
                val value = pref.getBoolean(key, default)
                downloadManager?.setConcurrentPages(if (value) 4 else 1)
            }
            getString(R.string.settings_downloader_retry_key) -> {
                val default = getString(R.string.settings_downloader_retry_default) == "true"
                val value = pref.getBoolean(key, default)
                downloadManager?.setRetryOnError(value)
            }
            getString(R.string.settings_downloader_wifi_only_key) -> {
                val default = getString(R.string.settings_downloader_wifi_only_default) == "true"
                val value = pref.getBoolean(key, default)
                downloadManager?.isWifiOnly(value)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

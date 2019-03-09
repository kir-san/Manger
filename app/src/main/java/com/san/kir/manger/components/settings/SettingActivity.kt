package com.san.kir.manger.components.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.DownloadService
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.ID
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.frameLayout

class SettingActivity : DrawerActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val content = ID.generate()

    override val _LinearLayout.customView: View
        get() = frameLayout { id = content }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.main_menu_settings)

        supportFragmentManager.beginTransaction()
                .replace(content, PrefFragment())
                .commit()

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.settings_downloader_parallel_key) -> {
                val default = getString(R.string.settings_downloader_parallel_default) == "true"
                val value = pref.getBoolean(key, default)
                DownloadService.setConcurrentPages(this, if (value) 4 else 1)
            }
            getString(R.string.settings_downloader_retry_key) -> {
                val default = getString(R.string.settings_downloader_retry_default) == "true"
                val value = pref.getBoolean(key, default)
                DownloadService.setRetryOnError(this, value)
            }
            getString(R.string.settings_downloader_wifi_only_key) -> {
                val default = getString(R.string.settings_downloader_wifi_only_default) == "true"
                val value = pref.getBoolean(key, default)
                DownloadService.isWifiOnly(this, value)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

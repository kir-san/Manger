package com.san.kir.manger.components.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.updateLayoutParams
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.sdk28.frameLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets

class SettingActivity : DrawerActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val content = ID.generate()

    override val _LinearLayout.customView: View
        get() = frameLayout {
            id = content
            doOnApplyWindowInstets { v, insets, _ ->
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = insets.systemWindowInsetBottom
                }
                insets
            }
        }

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
            getString(R.string.settings_app_dark_theme_key) -> {
                setNightMode(pref, key)
            }
        }
    }

    private fun setNightMode(pref: SharedPreferences, key: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (pref.getString(
                key, getString(R.string.settings_app_dark_theme_default)
            )) {
                getString(R.string.settings_app_dark_theme_dark) ->
                    AppCompatDelegate.MODE_NIGHT_YES
                getString(R.string.settings_app_dark_theme_white) ->
                    AppCompatDelegate.MODE_NIGHT_NO
                getString(R.string.settings_app_dark_theme_bettery) ->
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                getString(R.string.settings_app_dark_theme_system) ->
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else ->
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

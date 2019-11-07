package com.san.kir.manger.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.manger.R

abstract class BaseActivity : AppCompatActivity(), SharedPreferencesHolder {
    override val preferences: SharedPreferences by lazy { defaultSharedPreferences }
    override val editor: SharedPreferences.Editor by lazy { preferences.edit() }
    override val ctx: Context by lazy { this }

    override fun onDestroy() {
        super.onDestroy()
        finishEditor()
    }
}

abstract class ThemedActionBarActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.Theme_AppCompat else R.style.Theme_AppCompat_DayNight_DarkActionBar)

        super.onCreate(savedInstanceState)
    }
}

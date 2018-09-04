package com.san.kir.manger.extending

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.san.kir.manger.R
import org.jetbrains.anko.defaultSharedPreferences


abstract class BaseActivity : AppCompatActivity()

abstract class ThemedActionBarActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.Theme_AppCompat else R.style.Theme_AppCompat_DayNight_DarkActionBar)

        super.onCreate(savedInstanceState)
    }
}


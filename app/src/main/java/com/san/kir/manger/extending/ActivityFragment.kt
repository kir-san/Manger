package com.san.kir.manger.extending

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import com.san.kir.manger.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.coroutines.CoroutineContext


abstract class BaseActivity : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

abstract class ThemedActionBarActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.Theme_AppCompat else R.style.Theme_AppCompat_DayNight_DarkActionBar)

        super.onCreate(savedInstanceState)
    }
}

fun BaseActivity.getOrientation(): Int {
    val rotation = windowManager.defaultDisplay.rotation
    when (rotation) {
        Surface.ROTATION_0 -> {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        Surface.ROTATION_90 -> {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        Surface.ROTATION_180 -> {
            return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        }
        Surface.ROTATION_270 -> {
            return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
    }

    return ActivityInfo.SCREEN_ORIENTATION_FULL_USER
}


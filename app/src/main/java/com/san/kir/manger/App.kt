package com.san.kir.manger

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.evernote.android.job.JobManager
import com.github.kittinunf.fuel.core.FuelManager
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.schedule.ScheduleJob
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.utils.CATEGORY_ALL
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("unused")

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        themeActivation()

        externalDir = android.os.Environment.getExternalStorageDirectory()
        ManageSites.mSiteRepository = SiteRepository(this)
        FuelManager.instance.timeoutInMillisecond = TimeUnit.SECONDS.toMillis(30).toInt()
        JobManager.create(this).addJobCreator { ScheduleJob(it) }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Status.init(this)
        Translate.init(this)
        CATEGORY_ALL = getString(R.string.category_all)
    }

    private fun themeActivation() {
        val pref = defaultSharedPreferences
        val key = getString(R.string.settings_app_dark_theme_key)
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

    companion object {
        var externalDir: File? = null
    }
}

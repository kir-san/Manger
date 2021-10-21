package com.san.kir.manger

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.AppInitializer
import com.evernote.android.job.JobManager
import com.github.kittinunf.fuel.core.FuelManager
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.schedule.ScheduleJob
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.workmanager.CustomWorkManagerInitializer
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this)
            .initializeComponent(CustomWorkManagerInitializer::class.java)

        externalDir = android.os.Environment.getExternalStorageDirectory()
        FuelManager.instance.timeoutInMillisecond = TimeUnit.SECONDS.toMillis(30).toInt()
        JobManager.create(this).addJobCreator { ScheduleJob(it) }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Status.init(this)
        Translate.init(this)
        CATEGORY_ALL = getString(R.string.category_all)
    }

    companion object {
        var externalDir: File? = null
    }
}

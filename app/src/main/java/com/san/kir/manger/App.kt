package com.san.kir.manger

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.evernote.android.job.JobManager
import com.github.kittinunf.fuel.core.FuelManager
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.schedule.ScheduleJob
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.utils.CATEGORY_ALL
import java.util.concurrent.TimeUnit

@Suppress("unused")
class App : Application() {
    override fun onCreate() {
        super.onCreate()


        ManageSites.mSiteRepository = SiteRepository(this)
        FuelManager.instance.timeoutInMillisecond = TimeUnit.SECONDS.toMillis(30).toInt()
        JobManager.create(this).addJobCreator { ScheduleJob(it) }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Status.init(this)
        Translate.init(this)
        CATEGORY_ALL = getString(R.string.category_all)
    }
}

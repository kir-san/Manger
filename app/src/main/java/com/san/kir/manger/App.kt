package com.san.kir.manger

import android.app.Application
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import com.evernote.android.job.JobManager
import com.github.kittinunf.fuel.core.FuelManager
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.schedule.ScheduleJob
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.utils.CATEGORY_ALL
import java.io.File
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

    override fun getDatabasePath(name: String?): File {
        val dbFile = File(Environment.getExternalStorageDirectory(), name)
        if (!dbFile.parentFile.exists())
            dbFile.parentFile.mkdirs()
        return dbFile
    }

    override fun openOrCreateDatabase(
        name: String?,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase {
        checkProfileDatabase(name)
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory)
    }

    override fun openOrCreateDatabase(
        name: String?,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        checkProfileDatabase(name)
        return SQLiteDatabase.openOrCreateDatabase(
            getDatabasePath(name).path,
            factory,
            errorHandler
        )
    }

    private fun checkProfileDatabase(name: String?) {
        if (name == RoomDB.NAME) {
            val data = super.getDatabasePath("profile.db")
            val profile = getDatabasePath(RoomDB.NAME)
            if (data.exists() && !profile.exists()) {
                data.copyTo(profile)
            }
        }
    }
}

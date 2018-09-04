package com.san.kir.manger

import android.app.Application
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.support.v7.app.AppCompatDelegate
import com.evernote.android.job.JobManager
import com.san.kir.manger.components.schedule.ScheduleJob
import com.san.kir.manger.room.RoomDB
import java.io.File

class App : Application() {
    companion object {
        lateinit var exCacheDir: File
        lateinit var context: App
    }

    override fun onCreate() {
        super.onCreate()

        JobManager.create(this).addJobCreator { ScheduleJob(it) }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        context = this
        exCacheDir = externalCacheDir
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

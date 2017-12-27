package com.san.kir.manger

import android.app.Application
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.support.v7.app.AppCompatDelegate
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidActivityScope
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.lazy
import com.github.salomonbrys.kodein.singleton
import com.san.kir.manger.components.CatalogForOneSite.CatalogFilter
import com.san.kir.manger.components.CatalogForOneSite.FilterAdapter
import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.utils.log
import java.io.File

class App : Application(), KodeinAware {
    override val kodein: Kodein by Kodein.lazy {
        bind<List<CatalogFilter>>() with singleton {
            listOf(CatalogFilter("Жанры", FilterAdapter()),
                   CatalogFilter("Тип манги", FilterAdapter()))
        }
    }

    companion object {
        lateinit var exCacheDir: File
        lateinit var context: App
    }

    override fun onCreate() {
        super.onCreate()
        log("app is created")
        registerActivityLifecycleCallbacks(androidActivityScope.lifecycleManager)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        context = this
        exCacheDir = externalCacheDir

    }

    override fun getDatabasePath(name: String?): File {
        val dbfile = File(Environment.getExternalStorageDirectory(), name)
        if (!dbfile.parentFile.exists())
            dbfile.parentFile.mkdirs()
        return dbfile
    }

    override fun openOrCreateDatabase(name: String?,
                                      mode: Int,
                                      factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase {
        checkProfileDatabase(name)
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory)
    }

    override fun openOrCreateDatabase(name: String?,
                                      mode: Int,
                                      factory: SQLiteDatabase.CursorFactory?,
                                      errorHandler: DatabaseErrorHandler?): SQLiteDatabase {
        checkProfileDatabase(name)
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).path,
                                                   factory,
                                                   errorHandler)
    }

    private fun checkProfileDatabase(name: String?) {
        if (name == RoomDB.NAME) {
            val data = super.getDatabasePath("profile.db")
            val profile = getDatabasePath(RoomDB.NAME)
            if (data.exists() && !profile.exists()) {
                data.copyTo(profile)
                log("Ready copy from data to profile")
            }
        }
    }
}

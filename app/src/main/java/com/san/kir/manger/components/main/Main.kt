package com.san.kir.manger.components.main

import android.Manifest
import android.arch.persistence.room.Room
import android.content.pm.PackageManager
import android.os.Bundle
import com.san.kir.manger.App
import com.san.kir.manger.R
import com.san.kir.manger.components.downloadManager.DownloadService
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.ankoExtend.compatCheckSelfPermission
import com.san.kir.manger.extending.ankoExtend.compatRequestPermissions
import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.room.dao.MainMenuType
import com.san.kir.manger.room.dao.insertAsync
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.migrations.migrations
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.NAME_SHOW_CATEGORY
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startService


class Main : BaseActivity() {
    companion object {
        val db by lazy {
            Room.databaseBuilder(App.context, RoomDB::class.java, RoomDB.NAME)
                .addMigrations(*migrations)
                .allowMainThreadQueries()
                .build()
        }
    }
    /*private var bound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            log("onServiceDisconnected()")
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val downloadManager =
                    (service as DownloadService.LocalBinder).chapterLoader
            bound = true

//            downloadManager.pausedAllIfNotDownloading()
        }
    }*/

    private val updateApp = ManageSites.UpdateApp(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем есть ли разрешения на запись, если нет спрашиваем об этом
        val writeToExtStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (compatCheckSelfPermission(writeToExtStorage) != PackageManager.PERMISSION_GRANTED) {
            compatRequestPermissions(arrayOf(writeToExtStorage), 200)
        } else {
            init()
        }

        // Востановление настроек приложения
        with(defaultSharedPreferences) {
            if (!contains(NAME_SHOW_CATEGORY))
                edit().putBoolean(NAME_SHOW_CATEGORY, true).apply()
        }

//        val intent = Intent(this, DownloadService::class.java)
//        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    /*override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 200)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                longToast(R.string.main_permission_error)
                finishAffinity()
            }
    }

    private fun init() {
        createNeedFolders()
        createAndInitializeDb()
        updateApp.checkNewVersion()

        startService<DownloadService>()
        startActivity<LibraryActivity>()
    }

    private fun createNeedFolders() = DIR.ALL.forEach { dir -> createDirs(getFullPath(dir)) }

    private fun createAndInitializeDb() {
        if (db.categoryDao.loadCategories().isEmpty())
            db.categoryDao.insertAsync(Category(CATEGORY_ALL, 0))

        if (db.mainMenuDao.loadItems().isEmpty()) {
            MainMenuType.values()
                .filter { it != MainMenuType.Default }
                .forEachIndexed { index, type ->
                    db.mainMenuDao.insertAsync(
                        MainMenuItem(getString(type.stringId()), index, type)
                    )
                }
        } else {
            if (db.mainMenuDao
                    .loadItems()
                    .none { it.type == MainMenuType.Schedule }) {
                db.mainMenuDao.insertAsync(
                    MainMenuItem(
                        getString(MainMenuType.Schedule.stringId()),
                        100,
                        MainMenuType.Schedule
                    )
                )
            }

            db.mainMenuDao
                .loadItems()
                .forEach { item ->
                    item.name = getString(item.type.stringId())
                    db.mainMenuDao.updateAsync(item)
                }
        }
    }
}


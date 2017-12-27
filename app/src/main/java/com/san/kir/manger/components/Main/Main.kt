package com.san.kir.manger.components.Main

import android.Manifest
import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import com.san.kir.manger.App
import com.san.kir.manger.Extending.AnkoExtend.compatCheckSelfPermission
import com.san.kir.manger.Extending.AnkoExtend.compatRequestPermissions
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.components.DownloadManager.DownloadService
import com.san.kir.manger.components.Library.LibraryActivity
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.DAO.MainMenuType
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.NAME_SHOW_CATEGORY
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity


class Main : BaseActivity() {
    companion object {
        val db by lazy {
            Room.databaseBuilder(App.context, RoomDB::class.java, RoomDB.NAME)
                    .addMigrations(*RoomDB.Migrate.migrations)
                    .allowMainThreadQueries()
                    .build()
        }
    }
    private var bound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            log("onServiceDisconnected()")
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val downloadManager =
                    (service as DownloadService.LocalBinder).service.downloadManager
            bound = true

            async {
                val dao = Main.db.downloadDao
                dao.loadItems().filter {
                    it.status == DownloadStatus.loading ||
                            it.status == DownloadStatus.unknown
                }.forEach {
                    if (!downloadManager.hasTask(it)) {
                        log("$it")
                        it.status = DownloadStatus.pause
                        dao.update(it)
                    }
                }
            }
        }
    }

    val updateApp = ManageSites.UpdateApp(this)

    @SuppressLint("MissingSuperCall")
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

        updateApp.checkNewVersion()

        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == 200)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                longToast("Без этого разрешения от приложения мало толку")
                finishAffinity()
            }
    }

    private fun init() {
        createNeedFolders()
        createAndInitializeDb()

        startActivity<LibraryActivity>()
    }

    private fun createNeedFolders() = DIR.ALL.forEach { dir -> createDirs(getFullPath(dir)) }

    private fun createAndInitializeDb() {
        if (db.categoryDao.loadCategories().isEmpty())
            db.categoryDao.insert(Category(CATEGORY_ALL, 0))

        if (db.mainMenuDao.loadItems().isEmpty()) {
            MainMenuType.values()
                    .forEachIndexed { index, type ->
                        if (type != MainMenuType.Default)
                            db.mainMenuDao.insert(MainMenuItem(getString(type.stringId()),
                                                               index,
                                                               type))
                    }
        } else {
            db.mainMenuDao
                    .loadItems()
                    .forEach { item ->
                        item.name = getString(item.type.stringId())
                        db.mainMenuDao.update(item)
                    }
        }
    }
}


package com.san.kir.manger.components.main

import android.Manifest
import android.arch.persistence.room.Room
import android.content.pm.PackageManager
import android.os.Bundle
import com.san.kir.manger.App
import com.san.kir.manger.R
import com.san.kir.manger.components.downloadManager.DownloadService
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.schedule.ScheduleManager
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
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем есть ли разрешения на запись, если нет спрашиваем об этом
        val writeToExtStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (compatCheckSelfPermission(writeToExtStorage) != PackageManager.PERMISSION_GRANTED) {
            compatRequestPermissions(arrayOf(writeToExtStorage), 200)
        } else {
            init()
        }
    }

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
        restoreSchedule()

        startService<DownloadService>()
        startActivity<LibraryActivity>()
    }

    private fun restoreSchedule() {
        val man = ScheduleManager(this)
        db.plannedDao.loadPTasks().filter { it.isEnabled }.forEach { man.add(it) }
    }

    private fun createNeedFolders() = DIR.ALL.forEach { dir -> createDirs(getFullPath(dir)) }

    private fun createAndInitializeDb() {
        insertCategoryAll()
        insertMenuItems()
        insertMangaIntoStatistic()
    }

    private fun insertMangaIntoStatistic() {
        if (db.statisticDao.loadItems().isEmpty()) {
            db.mangaDao.loadAllManga().forEach { manga ->
                db.statisticDao.insertAsync(MangaStatistic(manga = manga.unic))
            }
        } else {
            val stats = db.statisticDao.loadItems()
            val new = db.mangaDao.loadAllManga()
                .filter { manga -> !stats.any { it.manga == manga.unic } }
            if (new.isNotEmpty()) {
                new.forEach { db.statisticDao.insertAsync(MangaStatistic(manga = it.unic)) }
            }
        }
    }

    private fun insertMenuItems() {
        if (db.mainMenuDao.loadItems().isEmpty()) {
            MainMenuType.values()
                .filter { it != MainMenuType.Default }
                .forEachIndexed { index, type ->
                    db.mainMenuDao.insertAsync(
                        MainMenuItem(getString(type.stringId()), index, type)
                    )
                }
        } else {
            val items = db.mainMenuDao.loadItems()
            MainMenuType.values().filter { type ->
                items.none { it.type == type }
            }.forEach {
                if (it != MainMenuType.Default)
                    db.mainMenuDao.insertAsync(
                        MainMenuItem(
                            getString(it.stringId()),
                            100,
                            it
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

    private fun insertCategoryAll() {
        if (db.categoryDao.loadCategories().isEmpty())
            db.categoryDao.insertAsync(Category(CATEGORY_ALL, 0))
    }
}


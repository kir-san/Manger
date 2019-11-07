package com.san.kir.manger.components.drawer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.startActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.schedule.ScheduleManager
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MainMenuRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.services.MigrateLatestChapterToChapterService
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.compatCheckSelfPermission
import com.san.kir.manger.utils.extensions.compatRequestPermissions
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.startForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Main : BaseActivity() {
    private val mCategoryRepository by lazy { CategoryRepository(this) }
    private val mMainMenuRepository by lazy { MainMenuRepository(this) }
    private val mStatisticRepository by lazy { StatisticRepository(this) }
    private val mMangaRepository by lazy { MangaRepository(this) }
    private val mPlannedRepository by lazy { PlannedRepository(this) }
    private val mSiteRepository by lazy { SiteRepository(this) }

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
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == 200)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                longToast(R.string.main_permission_error)
                finishAffinity()
            }
    }

    private fun init() = lifecycleScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.Default) {
            createNeedFolders()
            insertCategoryAll()
            insertMenuItems()
            insertMangaIntoStatistic()
            restoreSchedule()
            checkSiteCatalogs()
        }

        startForegroundService<MigrateLatestChapterToChapterService>()
        startActivity<LibraryActivity>()
    }

    private fun createNeedFolders() {
        DIR.ALL.forEach { dir -> getFullPath(dir).createDirs() }
    }

    private fun insertCategoryAll() {
        if (mCategoryRepository.getItems().isEmpty())
            mCategoryRepository.insert(Category(CATEGORY_ALL, 0))
    }

    private fun insertMenuItems() {
        if (mMainMenuRepository.getItems().isEmpty()) {
            MainMenuType.values()
                .filter { it != MainMenuType.Default }
                .forEachIndexed { index, type ->
                    mMainMenuRepository.insert(
                        MainMenuItem(getString(type.stringId()), index, type)
                    )
                }
        } else {
            val items = mMainMenuRepository.getItems()
            MainMenuType.values()
                .filter { type ->
                    items.none { it.type == type }
                }
                .forEach {
                    if (it != MainMenuType.Default)
                        mMainMenuRepository.insert(
                            MainMenuItem(
                                getString(it.stringId()),
                                100,
                                it
                            )
                        )
                }

            mMainMenuRepository.update(*mMainMenuRepository
                .getItems()
                .onEach { item ->
                    item.name = getString(item.type.stringId())
                }
                .toTypedArray())
        }
    }

    private fun insertMangaIntoStatistic() {
        if (mStatisticRepository.getItems().isEmpty()) {
            mMangaRepository.getItems().forEach { manga ->
                mStatisticRepository.insert(MangaStatistic(manga = manga.unic))
            }
        } else {
            val stats = mStatisticRepository.getItems()
            val new = mMangaRepository.getItems()
                .filter { manga -> !stats.any { it.manga == manga.unic } }
            if (new.isNotEmpty()) {
                new.forEach { mStatisticRepository.insert(MangaStatistic(manga = it.unic)) }
            }
        }
    }

    private fun restoreSchedule() {
        val man = ScheduleManager()
        mPlannedRepository.getItems().filter { it.isEnabled }.forEach { man.add(it) }
    }

    private suspend fun checkSiteCatalogs() {
        var dbSites = mSiteRepository.getItems()
        val appSites = ManageSites.CATALOG_SITES


        val deletingSites =
            dbSites.filter { dbSite -> appSites.none { appSite -> dbSite.name == appSite.name } }

        if (deletingSites.isNotEmpty()) {
            mSiteRepository.delete(*deletingSites.toTypedArray())
            dbSites = mSiteRepository.getItems()
        }

        val creatingSite =
            appSites.filter { appSite -> dbSites.none { dbSite -> appSite.name == dbSite.name } }

        if (creatingSite.isNotEmpty()) {
            creatingSite.forEach { site ->
                mSiteRepository.insert(
                    Site(
                        id = 0,
                        name = site.name,
                        host = site.host,
                        catalogName = site.catalogName,
                        volume = site.volume,
                        oldVolume = site.oldVolume,
                        siteID = site.id
                    )
                )
            }
        }
    }
}

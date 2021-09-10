package com.san.kir.manger.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.components.schedule.ScheduleManager
import com.san.kir.manger.data.datastore.ChaptersRepository
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.data.datastore.chaptersStore
import com.san.kir.manger.data.datastore.mainStore
import com.san.kir.manger.room.dao.MainMenuDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.dao.StatisticDao
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.MainMenuType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FirstInitAppWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val mainMenuDao: MainMenuDao,
    private val statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
    private val plannedDao: PlannedDao,
    private val siteDao: SiteDao,
    private val siteCatalogsManager: SiteCatalogsManager,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            updateMenuItems()
            insertMangaIntoStatistic()
            restoreSchedule()
            checkSiteCatalogs()
            setDefaultValueForStore()
        }.onFailure { return Result.retry() }
        return Result.success()
    }

    private suspend fun updateMenuItems() {
        val items = mainMenuDao.getItems()
        MainMenuType.values()
            .filter { type ->
                items.none { it.type == type }
            }
            .forEach {
                if (it != MainMenuType.Default)
                    mainMenuDao.insert(
                        MainMenuItem(
                            applicationContext.getString(it.stringId()),
                            100,
                            it
                        )
                    )
            }

        mainMenuDao.update(*mainMenuDao
            .getItems()
            .onEach { item ->
                item.name = applicationContext.getString(item.type.stringId())
            }
            .toTypedArray())
    }

    private suspend fun insertMangaIntoStatistic() {
        if (statisticDao.getItems().isEmpty()) {
            mangaDao.getItems().forEach { manga ->
                statisticDao.insert(MangaStatistic(manga = manga.unic))
            }
        } else {
            val stats = statisticDao.getItems()
            val new = mangaDao.getItems()
                .filter { manga -> !stats.any { it.manga == manga.unic } }
            if (new.isNotEmpty()) {
                new.forEach { statisticDao.insert(MangaStatistic(manga = it.unic)) }
            }
        }
    }

    private fun restoreSchedule() {
        val man = ScheduleManager()
        plannedDao.getItems().filter { it.isEnabled }.forEach { man.add(it) }
    }

    private suspend fun checkSiteCatalogs() {
        var dbSites = siteDao.getItems()


        val deletingSites =
            dbSites.filter { dbSite -> siteCatalogsManager.catalog.none { appSite -> dbSite.name == appSite.name } }

        if (deletingSites.isNotEmpty()) {
            siteDao.delete(*deletingSites.toTypedArray())
            dbSites = siteDao.getItems()
        }

        val creatingSite =
            siteCatalogsManager.catalog.filter { appSite -> dbSites.none { dbSite -> appSite.name == dbSite.name } }

        if (creatingSite.isNotEmpty()) {
            creatingSite.forEach { site ->
                siteDao.insert(
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

    private suspend fun setDefaultValueForStore() {
        val chStore = ChaptersRepository(applicationContext.chaptersStore)
        chStore.setFilter(ChapterFilter.ALL_READ_ASC.name)
        chStore.setIndividualFilter(true)
        chStore.setTitleVisibility(true)

        val mStore = MainRepository(applicationContext.mainStore)
        mStore.setShowCategory(true)
    }

    companion object {
        const val tag = "firstInitApp"

        fun addTask(ctx: Context): Operation {
            val task = OneTimeWorkRequestBuilder<FirstInitAppWorker>()
                .addTag(tag)
                .build()
            return WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}

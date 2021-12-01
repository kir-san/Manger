package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.manger.Viewer
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.data.datastore.ChaptersRepository
import com.san.kir.manger.data.datastore.DownloadRepository
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.data.datastore.ViewerRepository
import com.san.kir.manger.data.room.dao.MainMenuDao
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.dao.PlannedDao
import com.san.kir.manger.data.room.dao.SiteDao
import com.san.kir.manger.data.room.dao.StatisticDao
import com.san.kir.manger.data.room.entities.MainMenuItem
import com.san.kir.manger.data.room.entities.MangaStatistic
import com.san.kir.manger.data.room.entities.Site
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.MainMenuType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FirstInitAppWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted params: WorkerParameters,
    private val mainMenuDao: MainMenuDao,
    private val statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
    private val plannedDao: PlannedDao,
    private val siteDao: SiteDao,
    private val siteCatalogsManager: SiteCatalogsManager,
    private val chapterStore: ChaptersRepository,
    private val mainStore: MainRepository,
    private val downloadStore: DownloadRepository,
    private val viewerStore: ViewerRepository,
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
                statisticDao.insert(MangaStatistic(manga = manga.name))
            }
        } else {
            val stats = statisticDao.getItems()
            val new = mangaDao.getItems()
                .filter { manga -> !stats.any { it.manga == manga.name } }
            if (new.isNotEmpty()) {
                new.forEach { statisticDao.insert(MangaStatistic(manga = it.name)) }
            }
        }
    }

    private fun restoreSchedule() {
        plannedDao.getItems().filter { it.isEnabled }.forEach { ScheduleWorker.addTask(ctx, it) }
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
                        oldVolume = site.volume,
                        siteID = site.id
                    )
                )
            }
        }
    }

    private suspend fun setDefaultValueForStore() {
        chapterStore.setFilter(ChapterFilter.ALL_READ_ASC.name)
        chapterStore.setIndividualFilter(true)
        chapterStore.setTitleVisibility(true)

        mainStore.setShowCategory(true)
        mainStore.setTheme(true)

        downloadStore.setConcurrent(true)
        downloadStore.setRetry(false)
        downloadStore.setWifi(false)

        viewerStore.setOrientation(Viewer.Orientation.AUTO_LAND)
        viewerStore.setCutOut(true)
        viewerStore.setControl(taps = false, swipes = true, keys = false)
        viewerStore.setWithoutSaveFiles(false)
    }

    companion object {
        const val tag = "firstInitApp"

        fun addTask(ctx: Context): Operation {
            val task = OneTimeWorkRequestBuilder<FirstInitAppWorker>()
                .addTag(tag)
                .build()
            return WorkManager.getInstance(ctx).enqueueUniqueWork(
                tag + "Unique",
                ExistingWorkPolicy.KEEP,
                task)
        }
    }
}

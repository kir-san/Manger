package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.base.Site
import com.san.kir.data.models.datastore.Viewer
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.data.store.ChaptersStore
import com.san.kir.data.store.DownloadStore
import com.san.kir.data.store.MainStore
import com.san.kir.data.store.ViewerStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

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
    private val chapterStore: ChaptersStore,
    private val mainStore: MainStore,
    private val downloadStore: DownloadStore,
    private val viewerStore: ViewerStore,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            insertMangaIntoStatistic()
            restoreSchedule()
            checkSiteCatalogs()
            setDefaultValueForStore()
        }.onFailure { return Result.retry() }
        return Result.success()
    }


    private suspend fun insertMangaIntoStatistic() {
        if (statisticDao.getItems().isEmpty()) {
            mangaDao.getItems().forEach { manga ->
                statisticDao.insert(Statistic(manga = manga.name))
            }
        } else {
            val stats = statisticDao.getItems()
            val new = mangaDao.getItems()
                .filter { manga -> !stats.any { it.manga == manga.name } }
            if (new.isNotEmpty()) {
                new.forEach { statisticDao.insert(Statistic(manga = it.name)) }
            }
        }
    }

    private suspend fun restoreSchedule() {
        plannedDao.loadExtItems().first().filter { it.isEnabled }.forEach { ScheduleWorker.addTask(ctx, it) }
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
        chapterStore.setFilter(com.san.kir.core.support.ChapterFilter.ALL_READ_ASC.name)
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

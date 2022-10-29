package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.background.works.ScheduleWorker
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Site
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class FirstInitAppWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted params: WorkerParameters,
    private val statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
    private val plannedDao: PlannedDao,
    private val siteDao: SiteDao,
    private val siteCatalogsManager: SiteCatalogsManager,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            insertMangaIntoStatistic()
            restoreSchedule()
            checkSiteCatalogs()
        }.onFailure { return Result.retry() }
        return Result.success()
    }


    // Добавление недостающей статистики
    private suspend fun insertMangaIntoStatistic() {
        if (statisticDao.items().isEmpty()) {
            // если совсем нет статистики, то добавляем для каждой манги
            mangaDao.items().forEach { manga ->
                statisticDao.insert(Statistic(mangaId = manga.id))
            }
        } else {
            // иначе только для отсутствующей
            val stats = statisticDao.items()
            val new = mangaDao.items()
                .filter { manga -> !stats.any { it.mangaId == manga.id } }
            if (new.isNotEmpty()) {
                new.forEach { statisticDao.insert(Statistic(mangaId = it.id)) }
            }
        }
    }

    private suspend fun restoreSchedule() {
        plannedDao.loadSimpleItems().first().filter { it.isEnabled }
            .forEach { ScheduleWorker.addTask(ctx, it) }
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

    companion object {
        const val tag = "firstInitApp"

        fun addTask(ctx: Context): Operation {
            val task = OneTimeWorkRequestBuilder<FirstInitAppWorker>()
                .addTag(tag)
                .build()
            return WorkManager.getInstance(ctx).enqueueUniqueWork(
                tag + "Unique",
                ExistingWorkPolicy.KEEP,
                task
            )
        }
    }
}

package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.schedule.ScheduleManager
import com.san.kir.manger.data.datastore.ChaptersRepository
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.data.datastore.chaptersStore
import com.san.kir.manger.data.datastore.mainStore
import com.san.kir.manger.repositories.MainMenuRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.MainMenuType

class FirstInitAppWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    private val mMainMenuRepository by lazy { MainMenuRepository(applicationContext) }
    private val mStatisticRepository by lazy { StatisticRepository(applicationContext) }
    private val mMangaRepository by lazy { MangaRepository(applicationContext) }
    private val mPlannedRepository by lazy { PlannedRepository(applicationContext) }
    private val mSiteRepository by lazy { SiteRepository(applicationContext) }

    override suspend fun doWork(): Result {
        updateMenuItems()
        insertMangaIntoStatistic()
        restoreSchedule()
        checkSiteCatalogs()
        setDefaultValueForStore()
        return Result.success()
    }

    private suspend fun updateMenuItems() {
        val items = mMainMenuRepository.getItems()
        MainMenuType.values()
            .filter { type ->
                items.none { it.type == type }
            }
            .forEach {
                if (it != MainMenuType.Default)
                    mMainMenuRepository.insert(
                        MainMenuItem(
                            applicationContext.getString(it.stringId()),
                            100,
                            it
                        )
                    )
            }

        mMainMenuRepository.update(*mMainMenuRepository
            .getItems()
            .onEach { item ->
                item.name = applicationContext.getString(item.type.stringId())
            }
            .toTypedArray())
    }

    private suspend fun insertMangaIntoStatistic() {
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

        fun addTask(ctx: Context) {
            val task = OneTimeWorkRequestBuilder<FirstInitAppWorker>()
                .addTag(tag)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}

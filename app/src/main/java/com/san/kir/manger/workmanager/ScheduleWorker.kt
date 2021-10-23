package com.san.kir.manger.workmanager

import android.app.AlarmManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.R
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.room.entities.PlannedTaskColumn
import com.san.kir.manger.room.entities.mangaList
import com.san.kir.manger.services.AppUpdateService
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.enums.PlannedPeriod
import com.san.kir.manger.utils.enums.PlannedType
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.startForegroundService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val plannedDao: PlannedDao,
    private val mangaDao: MangaDao,
    private val siteDao: SiteDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val id = inputData.getLong(PlannedTaskColumn.tableName, -1L)

        log("doWork $id")
        if (id != -1L) {
            kotlin.runCatching {

                val task = plannedDao.getItem(id)

                when (task.type) {
                    PlannedType.MANGA -> {
                        val manga = mangaDao.getItem(task.manga)
                        applicationContext
                            .startForegroundService<MangaUpdaterService>(
                                MangaColumn.tableName to manga
                            )
                    }
                    PlannedType.GROUP -> {
                        task.mangaList.forEach { unic ->
                            val manga = mangaDao.getItem(unic)
                            applicationContext
                                .startForegroundService<MangaUpdaterService>(
                                    MangaColumn.tableName to manga
                                )
                        }

                    }
                    PlannedType.CATEGORY -> {
                        val mangas =
                            if (CATEGORY_ALL == task.category) mangaDao.getItems()
                            else mangaDao.getMangaWhereCategoryNotAll(task.category)
                        mangas.forEach {
                            applicationContext
                                .startForegroundService<MangaUpdaterService>(
                                    MangaColumn.tableName to it
                                )
                        }
                    }
                    PlannedType.CATALOG -> {
                        val catalog = siteDao.getItem(task.catalog)
                        if (catalog != null && !CatalogForOneSiteUpdaterService.isContain(catalog.catalogName)) {
                            applicationContext
                                .startForegroundService<CatalogForOneSiteUpdaterService>(
                                    "catalogName" to catalog.catalogName
                                )
                        } else {
                        }
                    }
                    PlannedType.APP -> {
                        applicationContext
                            .startForegroundService<AppUpdateService>()
                    }
                }
            }.fold(
                onSuccess = {
                    Result.success()
                },
                onFailure = {
                    it.printStackTrace()
                    Result.failure()
                }
            )
        } else {
            Result.retry()
        }
    }

    companion object {
        const val tag = "scheduleWork"
        private const val dayPeriod = AlarmManager.INTERVAL_DAY
        private const val weekPeriod = dayPeriod * 7

        fun addTask(ctx: Context, item: PlannedTask) {


            val delay = getDelay(item)
            log("delay $delay ")
            val perTask = PeriodicWorkRequestBuilder<ScheduleWorker>(
                if (item.period == PlannedPeriod.DAY) 1L else 7L, TimeUnit.DAYS,
                1L, TimeUnit.MINUTES,
            )
                .addTag(tag + item.id)
                .setInputData(workDataOf(PlannedTaskColumn.tableName to item.id))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                /*.setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )*/
                .build()
            val oneTask = OneTimeWorkRequestBuilder<ScheduleWorker>()
                .addTag(tag + item.id)
                .setInputData(workDataOf(PlannedTaskColumn.tableName to item.id))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(ctx).enqueue(listOf(oneTask, perTask))
        }

        fun cancelTask(ctx: Context, item: PlannedTask) {
            val exe = ContextCompat.getMainExecutor(ctx)

            WorkManager.getInstance(ctx)
                .cancelAllWorkByTag(tag + item.id)
                .result
                .addListener(
                    {
                        when (item.type) {
                            PlannedType.MANGA ->
                                ctx.longToast(
                                    ctx.getString(
                                        R.string.schedule_manager_cancel_manga,
                                        item.manga
                                    )
                                )
                            PlannedType.CATEGORY ->
                                ctx.longToast(
                                    ctx.getString(
                                        R.string.schedule_manager_cancel_category,
                                        item.category
                                    )
                                )
                            PlannedType.GROUP ->
                                ctx.longToast(
                                    ctx.getString(
                                        R.string.schedule_manager_cancel_group,
                                        item.groupName
                                    )
                                )
                            PlannedType.CATALOG ->
                                ctx.longToast(
                                    ctx.getString(
                                        R.string.schedule_manager_cancel_catalog,
                                        item.catalog
                                    )
                                )
                            PlannedType.APP ->
                                ctx.longToast(
                                    ctx.getString(
                                        R.string.schedule_manager_cancel_app
                                    )
                                )
                        }
                    },
                    exe
                )

        }

        private fun getDelay(item: PlannedTask): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, item.hour)
            calendar.set(Calendar.MINUTE, item.minute)
            calendar.set(Calendar.SECOND, 0)
            if (item.period == PlannedPeriod.WEEK) {
                calendar.set(Calendar.DAY_OF_WEEK, item.dayOfWeek.order)
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.timeInMillis += weekPeriod
                }
            }

            return if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.timeInMillis + dayPeriod - System.currentTimeMillis()
            } else {
                calendar.timeInMillis - System.currentTimeMillis()
            }
        }


    }
}


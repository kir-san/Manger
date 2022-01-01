package com.san.kir.manger.foreground_work.workmanager

import android.app.AlarmManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.models.PlannedTask
import com.san.kir.data.models.columns.PlannedTaskColumn
import com.san.kir.data.models.mangaList
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.AppUpdateService
import com.san.kir.manger.foreground_work.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.foreground_work.services.MangaUpdaterService
import com.san.kir.core.utils.longToast
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
                        val manga = mangaDao.item(task.manga)
                        MangaUpdaterService.add(applicationContext, manga)
                    }
                    PlannedType.GROUP -> {
                        task.mangaList.forEach { unic ->
                            val manga = mangaDao.item(unic)
                            MangaUpdaterService.add(applicationContext, manga)
                        }

                    }
                    PlannedType.CATEGORY -> {
                        val mangas =
                            if (applicationContext.CATEGORY_ALL == task.category) mangaDao.getItems()
                            else mangaDao.itemsWhereCategoryNotAll(task.category)
                        mangas.forEach {
                            MangaUpdaterService.add(applicationContext, it)
                        }
                    }
                    PlannedType.CATALOG -> {
                        val catalog = siteDao.getItem(task.catalog)
                        if (catalog != null &&
                            !CatalogForOneSiteUpdaterService.isContain(catalog.catalogName)
                        ) {
                            CatalogForOneSiteUpdaterService.add(applicationContext,
                                catalog.catalogName)
                        }
                    }
                    PlannedType.APP -> {
                        AppUpdateService.start(applicationContext)
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
                .addListener({
                    when (item.type) {
                        PlannedType.MANGA ->
                            ctx.longToast(R.string.schedule_manager_cancel_manga, item.manga)
                        PlannedType.CATEGORY ->
                            ctx.longToast(R.string.schedule_manager_cancel_category, item.category)
                        PlannedType.GROUP ->
                            ctx.longToast(R.string.schedule_manager_cancel_group, item.groupName)
                        PlannedType.CATALOG ->
                            ctx.longToast(R.string.schedule_manager_cancel_catalog, item.catalog)
                        PlannedType.APP ->
                            ctx.longToast(R.string.schedule_manager_cancel_app)
                    }
                }, exe)

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


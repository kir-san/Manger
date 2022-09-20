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
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.utils.longToast
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.db.dao.defaultCategory
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.base.PlannedTaskBase
import com.san.kir.data.models.base.mangaList
import com.san.kir.manger.R
import com.san.kir.background.services.AppUpdateService
import com.san.kir.manger.foreground_work.services.CatalogForOneSiteUpdaterService
import com.san.kir.background.services.MangaUpdaterService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val plannedDao: PlannedDao,
    private val mangaDao: MangaDao,
    private val siteDao: SiteDao,
    private val categoryDao: CategoryDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(PlannedTask.tableName, -1L)

        Timber.v("doWork $id")
        if (id != -1L) {
            kotlin.runCatching {

                val task = plannedDao.itemById(id)

                when (task.type) {
                    PlannedType.MANGA -> {
                        val manga = mangaDao.itemByName(task.manga)
                        MangaUpdaterService.add(applicationContext, manga)
                    }
                    PlannedType.GROUP -> {
                        task.mangaList.forEach { unic ->
                            val manga = mangaDao.itemByName(unic)
                            MangaUpdaterService.add(applicationContext, manga)
                        }

                    }
                    PlannedType.CATEGORY -> {
                        val defaultCategory = categoryDao.defaultCategory(applicationContext)

                        val mangas =
                            if (defaultCategory.id == task.categoryId) mangaDao.items()
                            else mangaDao.itemsByCategoryId(task.categoryId)
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
                onSuccess = { return Result.success() },
                onFailure = {
                    it.printStackTrace()
                    return Result.failure()
                }
            )
        } else return Result.retry()
    }

    companion object {
        const val tag = "scheduleWork"
        private const val dayPeriod = AlarmManager.INTERVAL_DAY
        private const val weekPeriod = dayPeriod * 7

        fun addTask(ctx: Context, item: PlannedTaskBase) {

            val delay = getDelay(item)
            Timber.v("delay $delay ")
            val perTask = PeriodicWorkRequestBuilder<ScheduleWorker>(
                if (item.period == PlannedPeriod.DAY) 1L else 7L, TimeUnit.DAYS,
                1L, TimeUnit.MINUTES,
            )
                .addTag(tag + item.id)
                .setInputData(workDataOf(PlannedTask.tableName to item.id))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                /*.setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )*/
                .build()
            val oneTask = OneTimeWorkRequestBuilder<ScheduleWorker>()
                .addTag(tag + item.id)
                .setInputData(workDataOf(PlannedTask.tableName to item.id))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(ctx).enqueue(listOf(oneTask, perTask))
        }

        fun cancelTask(ctx: Context, item: PlannedTaskBase) {
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

        private fun getDelay(item: PlannedTaskBase): Long {
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


package com.san.kir.manger.components.schedule

import com.evernote.android.job.Job
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteUpdaterService
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.room.entities.mangaList
import com.san.kir.manger.services.AppUpdateService
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.enums.PlannedType
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.startForegroundService

class ScheduleJob(private val tag: String) : Job() {
    override fun onRunJob(params: Params): Result {
        val plannedRepository = PlannedRepository(context)
        val taskId = tag.toLong()
        val task =plannedRepository.getItem(taskId)

        try {
            when (task.type) {
                PlannedType.MANGA -> {
                    val manga = MangaRepository(context).getItem(task.manga)
                    context.startForegroundService<MangaUpdaterService>(MangaColumn.tableName to manga)
                    ScheduleManager().add(task)
                }
                PlannedType.CATEGORY -> {
                    val categories = MangaRepository(context).getItemsWhere(task.category)
                    categories.forEach {
                        context.startForegroundService<MangaUpdaterService>(MangaColumn.tableName to it)
                    }
                    ScheduleManager().add(task)
                }
                PlannedType.GROUP -> {
                    val group = task.mangaList
                    val mangaRepository = MangaRepository(context)
                    group.forEach { unic ->
                        val manga = mangaRepository.getItem(unic)
                        context.startForegroundService<MangaUpdaterService>(MangaColumn.tableName to manga)
                    }
                    ScheduleManager().add(task)
                }
                PlannedType.CATALOG -> {
                    val catalog = SiteRepository(context).getItem(task.catalog)
                    if (catalog != null && !CatalogForOneSiteUpdaterService.isContain(catalog.catalogName))
                        context.startForegroundService<CatalogForOneSiteUpdaterService>("catalogName" to catalog.catalogName)
                }
                PlannedType.APP -> {
                    context.startForegroundService<AppUpdateService>()
                }
                else -> {
                    log("Тип не соответсвует действительности")
                    return Result.FAILURE
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return Result.FAILURE
        }


        return Result.SUCCESS
    }
}


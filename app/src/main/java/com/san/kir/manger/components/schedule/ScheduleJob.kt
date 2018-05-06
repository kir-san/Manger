package com.san.kir.manger.components.schedule

import com.evernote.android.job.Job
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.startForegroundService
import com.san.kir.manger.room.dao.loadMangaWhereCategory
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.room.models.PlannedType
import com.san.kir.manger.room.models.mangaList
import com.san.kir.manger.utils.MangaUpdaterService
import com.san.kir.manger.utils.log

class ScheduleJob(private val tag: String) : Job() {
    override fun onRunJob(params: Params): Result {
        val taskId = tag.toLong()
        val task = Main.db.plannedDao.loadPlannedTask(taskId)

        try {
            when (task.type) {
                PlannedType.MANGA -> {
                    val manga = Main.db.mangaDao.loadManga(task.manga)
                    context.startForegroundService<MangaUpdaterService>(MangaColumn.tableName to manga)
                    ScheduleManager(context).add(task)
                }
                PlannedType.CATEGORY -> {
                    val categories = Main.db.mangaDao.loadMangaWhereCategory(task.category)
                    categories.forEach {
                        context.startForegroundService<MangaUpdaterService>(MangaColumn.tableName to it)
                    }
                    ScheduleManager(context).add(task)
                }
                PlannedType.GROUP -> {
                    val group = task.mangaList
                    group.forEach { unic ->
                        val manga = Main.db.mangaDao.loadManga(unic)
                        context.startForegroundService<MangaUpdaterService>(MangaColumn.tableName to manga)
                    }
                    ScheduleManager(context).add(task)
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


package com.san.kir.manger.components.schedule

import android.app.AlarmManager
import android.content.Context
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.san.kir.manger.R
import com.san.kir.manger.room.models.PlannedPeriod
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.PlannedType
import com.san.kir.manger.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.longToast
import java.util.*

class ScheduleManager {
    private val dayPeriod = AlarmManager.INTERVAL_DAY
    private val weekPeriod = dayPeriod * 7

    fun add(task: PlannedTask) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, task.hour)
        calendar.set(Calendar.MINUTE, task.minute)
        calendar.set(Calendar.SECOND, 0)
        if (task.period == PlannedPeriod.WEEK) {
            calendar.set(Calendar.DAY_OF_WEEK, task.dayOfWeek)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.timeInMillis += weekPeriod
            }
        }

        val trigger = if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.timeInMillis + dayPeriod - System.currentTimeMillis()
        } else {
            calendar.timeInMillis - System.currentTimeMillis()
        }

        JobRequest.Builder(task.addedTime.toString())
            .setExecutionWindow(trigger, trigger + 60000L)
            .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
            .setUpdateCurrent(true)
            .build()
            .schedule()
    }

    fun cancel(context: Context, plannedTask: PlannedTask) {
        val tasks = JobManager.instance().cancelAllForTag(plannedTask.addedTime.toString())
        if (tasks >= 1) {
            GlobalScope.launch(Dispatchers.Main) {
                when (plannedTask.type) {
                    PlannedType.MANGA ->
                        context.longToast(
                            context.getString(
                                R.string.schedule_manager_cancel_manga,
                                plannedTask.manga
                            )
                        )
                    PlannedType.CATEGORY ->
                        context.longToast(
                            context.getString(
                                R.string.schedule_manager_cancel_category,
                                plannedTask.category
                            )
                        )
                    PlannedType.GROUP ->
                        context.longToast(
                            context.getString(
                                R.string.schedule_manager_cancel_group,
                                plannedTask.groupName
                            )
                        )
                    PlannedType.CATALOG ->
                        context.longToast(
                            context.getString(
                                R.string.schedule_manager_cancel_catalog,
                                plannedTask.catalog
                            )
                        )
                    PlannedType.APP ->
                        context.longToast(
                            context.getString(
                                R.string.schedule_manager_cancel_app
                            )
                        )
                    else ->
                        log("Тип не соответсвует действительности")
                }
            }
        }
    }
}

package com.san.kir.manger.components.schedule

import android.app.AlarmManager
import android.content.Context
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.san.kir.manger.room.models.PlannedPeriod
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.PlannedType
import com.san.kir.manger.utils.log
import org.jetbrains.anko.longToast
import java.util.*

class ScheduleManager(private val context: Context) {
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

    fun cancel(plannedTask: PlannedTask) {
        val tasks = JobManager.instance().cancelAllForTag(plannedTask.addedTime.toString())
        if (tasks >= 1) {
            when (plannedTask.type) {
                PlannedType.MANGA ->
                    context.longToast("Было отменено обновление манги ${plannedTask.manga}")
                PlannedType.CATEGORY ->
                    context.longToast("Было отменено обновление сатегории ${plannedTask.category}")
                PlannedType.GROUP ->
                    context.longToast("Было отменено обновление группы ${plannedTask.groupName}")
                else ->
                    log("Тип не соответсвует действительности")
            }
        }
        log("canceled tasks = $tasks")
    }
}

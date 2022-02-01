package com.san.kir.manger.ui.application_navigation.schedule.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.models.extend.PlannedTaskExt
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.workmanager.ScheduleWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val context: Application,
    private val plannedDao: PlannedDao,
) : ViewModel() {
    var items by mutableStateOf(listOf<PlannedTask>())
        private set

    fun update(item: PlannedTaskExt, enable: Boolean) = viewModelScope.defaultLaunch {
        plannedDao.update(item.id, enable)

        if (enable)
            ScheduleWorker.addTask(context, item)
        else
            ScheduleWorker.cancelTask(context, item)
    }

    fun itemName(item: PlannedTaskExt) = when (item.type) {
        PlannedType.MANGA -> context.getString(R.string.planned_task_name_manga, item.manga)
        PlannedType.CATEGORY -> context.getString(
            R.string.planned_task_name_category,
            item.category
        )
        PlannedType.GROUP -> context.getString(
            R.string.planned_task_name_group,
            item.groupName
        )
        PlannedType.CATALOG -> context.getString(
            R.string.planned_task_name_catalog,
            item.catalog
        )
        PlannedType.APP -> context.getString(R.string.planned_task_name_app)
    }

    fun itemInfo(item: PlannedTaskExt): String {
        val dayText: String =
            if (item.period == PlannedPeriod.DAY) {
                context.getString(R.string.planned_task_update_text_day)
            } else {
                when (item.dayOfWeek) {
                    PlannedWeek.MONDAY -> context.getString(R.string.planned_task_update_text_monday)
                    PlannedWeek.TUESDAY -> context.getString(R.string.planned_task_update_text_tuesday)
                    PlannedWeek.WEDNESDAY -> context.getString(R.string.planned_task_update_text_wednesday)
                    PlannedWeek.THURSDAY -> context.getString(R.string.planned_task_update_text_thursday)
                    PlannedWeek.FRIDAY -> context.getString(R.string.planned_task_update_text_friday)
                    PlannedWeek.SATURDAY -> context.getString(R.string.planned_task_update_text_saturday)
                    PlannedWeek.SUNDAY -> context.getString(R.string.planned_task_update_text_sunday)
                }
            }
        return context.getString(
            R.string.planned_task_update_text_template,
            dayText,
            item.hour,
            String.format("%02d", item.minute)
        )
    }
}

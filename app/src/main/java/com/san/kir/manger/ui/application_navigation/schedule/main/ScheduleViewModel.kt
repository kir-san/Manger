package com.san.kir.manger.ui.application_navigation.schedule.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.R
import com.san.kir.manger.components.schedule.ScheduleManager
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.di.MainDispatcher
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.utils.enums.PlannedPeriod
import com.san.kir.manger.utils.enums.PlannedType
import com.san.kir.manger.utils.enums.PlannedWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val context: Application,
    private val plannedDao: PlannedDao,
    @DefaultDispatcher private val default: CoroutineDispatcher,
    @MainDispatcher main: CoroutineDispatcher,
    private val alarmManager: ScheduleManager,
) : ViewModel() {
    var items by mutableStateOf(listOf<PlannedTask>())
        private set

    init {
        plannedDao.loadItems()
            .distinctUntilChanged()
            .onEach { withContext(main) { items = it } }
            .launchIn(viewModelScope)
    }

    fun update(item: PlannedTask) = viewModelScope.launch(default) {
        plannedDao.update(item)

        if (item.isEnabled)
            alarmManager.add(item)
        else
            alarmManager.cancel(context, item)
    }

    fun itemName(item: PlannedTask): String {
        return when (item.type) {
            PlannedType.MANGA -> context.getString(R.string.planned_task_name_manga, item.manga)
            PlannedType.GROUP -> context.getString(
                R.string.planned_task_name_category,
                item.groupName
            )
            PlannedType.CATEGORY -> context.getString(
                R.string.planned_task_name_group,
                item.category
            )
            PlannedType.CATALOG -> context.getString(
                R.string.planned_task_name_catalog,
                item.catalog
            )
            PlannedType.APP -> context.getString(R.string.planned_task_name_app)
        }
    }

    fun itemInfo(item: PlannedTask): String {
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

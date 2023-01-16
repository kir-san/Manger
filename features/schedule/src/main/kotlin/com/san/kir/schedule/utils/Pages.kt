package com.san.kir.schedule.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.san.kir.schedule.R
import com.san.kir.schedule.ui.tasks.TasksScreen
import com.san.kir.schedule.ui.updates.UpdatesScreen

internal sealed class SchedulePages(
    val nameId: Int,
    val content: @Composable (navigateToItem: (Long) -> Unit) -> Unit
)

@Composable
internal fun pages() = remember { listOf(PlannedPage, UpdatePages) }

internal object PlannedPage : SchedulePages(
    nameId = R.string.planned_task_name,
    content = { nav -> TasksScreen(nav) }
)

internal object UpdatePages : SchedulePages(
    nameId = R.string.available_update_name,
    content = { UpdatesScreen() }
)

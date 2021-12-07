package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.san.kir.manger.R

sealed class SchedulePages(
    val nameId: Int,
    val content: @Composable (NavHostController) -> Unit
)

fun schedulePages() = listOf(PlannedPage, UpdatePages)

object PlannedPage : SchedulePages(
    nameId = R.string.planned_task_name,
    content = { nav -> PlannedContent(nav) }
)

object UpdatePages : SchedulePages(
    nameId = R.string.available_update_name,
    content = { UpdateContent() }
)

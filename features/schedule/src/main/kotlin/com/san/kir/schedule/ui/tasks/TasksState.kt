package com.san.kir.schedule.ui.tasks

import com.san.kir.core.utils.viewModel.ScreenState
import kotlinx.collections.immutable.ImmutableList


internal data class TasksState(
    val items: ImmutableList<Task>
) : ScreenState

internal data class Task(
    val id: Long,
    val name: String,
    val info: String,
    val isEnabled: Boolean
)

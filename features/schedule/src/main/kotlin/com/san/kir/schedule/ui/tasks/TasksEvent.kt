package com.san.kir.schedule.ui.tasks

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface TasksEvent : ScreenEvent {
    data class Update(val itemId: Long, val state: Boolean) : TasksEvent
}

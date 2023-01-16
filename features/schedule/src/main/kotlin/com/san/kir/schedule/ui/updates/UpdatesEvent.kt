package com.san.kir.schedule.ui.updates

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface UpdatesEvent : ScreenEvent {
    data class Update(val itemId: Long, val updateState: Boolean) : UpdatesEvent
}

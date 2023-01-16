package com.san.kir.catalog.ui.addOnline

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface AddOnlineEvent : ScreenEvent {
    data class Update(val text: String) : AddOnlineEvent
}

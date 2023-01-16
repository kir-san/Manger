package com.san.kir.features.shikimori.ui.accountScreen

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface AccountEvent : ScreenEvent {
    data object LogOut : AccountEvent
    data object CancelLogOut : AccountEvent
    data object Update : AccountEvent
}

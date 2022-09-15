package com.san.kir.features.shikimori.ui.accountScreen

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface AccountEvent : ScreenEvent {
    object LogOut : AccountEvent
    object CancelLogOut : AccountEvent
    object Update : AccountEvent
}

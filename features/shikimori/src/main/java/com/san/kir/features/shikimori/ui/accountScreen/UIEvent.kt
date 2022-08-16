package com.san.kir.features.shikimori.ui.accountScreen

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface UIEvent : ScreenEvent {
    object LogOut : UIEvent
    object CancelLogOut : UIEvent
    object Update : UIEvent
}

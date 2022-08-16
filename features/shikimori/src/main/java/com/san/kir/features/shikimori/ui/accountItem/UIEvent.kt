package com.san.kir.features.shikimori.ui.accountItem

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface UIEvent : ScreenEvent {
    object LogIn : UIEvent
    object LogOut : UIEvent
    object CancelLogOut : UIEvent
}

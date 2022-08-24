package com.san.kir.features.shikimori.ui.accountItem

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface AccountItemEvent : ScreenEvent {
    object LogIn : AccountItemEvent
    object LogOut : AccountItemEvent
    object CancelLogOut : AccountItemEvent
}

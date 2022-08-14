package com.san.kir.features.shikimori.ui.accountItem

import com.san.kir.core.utils.viewModel.Event

internal sealed interface UIEvent : Event {
    object LogIn : UIEvent
    object LogOut : UIEvent
    object CancelLogOut : UIEvent
}

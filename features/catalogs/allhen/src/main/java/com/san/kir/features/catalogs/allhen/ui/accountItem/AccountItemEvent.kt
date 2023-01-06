package com.san.kir.features.catalogs.allhen.ui.accountItem

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface AccountItemEvent : ScreenEvent {
    data object Update : AccountItemEvent
}

package com.san.kir.features.shikimori.ui.localItems

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface LocalItemsEvent : ScreenEvent {
    object Update : LocalItemsEvent
}

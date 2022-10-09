package com.san.kir.storage.ui.storage

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface StorageEvent : ScreenEvent {
    data class Set(val mangaId: Long, val hasUpdate: Boolean) : StorageEvent
    object DeleteAll : StorageEvent
    object DeleteRead : StorageEvent
}

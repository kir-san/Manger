package com.san.kir.storage.ui.storage

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface StorageEvent : ScreenEvent {
    data class Set(val mangaId: Long, val hasUpdate: Boolean) : StorageEvent
    data object DeleteAll : StorageEvent
    data object DeleteRead : StorageEvent
}

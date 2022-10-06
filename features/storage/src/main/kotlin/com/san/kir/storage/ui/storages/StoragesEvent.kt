package com.san.kir.storage.ui.storages

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.base.Storage

sealed interface StoragesEvent : ScreenEvent {
    data class Delete(val item: Storage) : StoragesEvent
}

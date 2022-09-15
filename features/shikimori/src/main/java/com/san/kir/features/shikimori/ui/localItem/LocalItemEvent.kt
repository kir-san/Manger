package com.san.kir.features.shikimori.ui.localItem

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.features.shikimori.logic.SyncDialogEvent

internal sealed interface LocalItemEvent : ScreenEvent {
    data class Update(val mangaId: Long? = null) : LocalItemEvent
    data class Sync(val event: SyncDialogEvent) : LocalItemEvent
}

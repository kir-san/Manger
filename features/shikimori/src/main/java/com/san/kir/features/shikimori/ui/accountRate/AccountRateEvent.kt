package com.san.kir.features.shikimori.ui.accountRate

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.logic.SyncDialogEvent

internal sealed interface AccountRateEvent : ScreenEvent {
    object ExistToggle : AccountRateEvent
    data class Update(val item: ShikimoriRate? = null, val id: Long? = null) : AccountRateEvent
    data class Sync(val event: SyncDialogEvent) : AccountRateEvent
}

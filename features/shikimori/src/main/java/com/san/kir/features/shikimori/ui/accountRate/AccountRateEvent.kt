package com.san.kir.features.shikimori.ui.accountRate

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.base.ShikimoriRate

internal sealed interface AccountRateEvent : ScreenEvent {
    object SyncCancel : AccountRateEvent

    data class SyncToggle(
        val item: ShikimoriMangaItem
    ) : AccountRateEvent

    data class SyncNext(
        val item: ShikimoriMangaItem, val onlineIsTruth: Boolean = false
    ) : AccountRateEvent

    object DialogDismiss : AccountRateEvent
    object ExistToggle : AccountRateEvent
    data class Update(val item: ShikimoriRate? = null, val id: Long? = null) : AccountRateEvent
}

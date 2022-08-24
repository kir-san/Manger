package com.san.kir.features.shikimori.ui.accountRate

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.base.ShikimoriRate

internal data class AccountRateState(
    val sync: SyncState,
    val dialog: DialogState,
    val profile: ProfileState,
    val manga: MangaState,
) : ScreenState


// Состояние связывания
sealed interface SyncState {
    object None : SyncState
    class Ok(val manga: ShikimoriMangaItem) : SyncState
    object Finding : SyncState
    class Founds(val items: List<ShikimoriMangaItem>) : SyncState
    class NotFounds(val name: String) : SyncState
}

// Запросы при выполнении связывания
sealed interface DialogState {
    object None : DialogState

    data class Init(
        val manga: ShikimoriMangaItem,
    ) : DialogState

    data class DifferentChapterCount(
        val manga: ShikimoriMangaItem,
        val local: Long,
        val online: Long,
    ) : DialogState

    data class DifferentReadCount(
        val manga: ShikimoriMangaItem,
        val local: Long,
        val online: Long,
    ) : DialogState

    object CancelSync : DialogState
}

sealed interface ProfileState {
    data class Ok(val rate: ShikimoriRate) : ProfileState
    object None : ProfileState
    object Load : ProfileState
}

sealed interface MangaState {
    data class Ok(val item: ShikimoriManga) : MangaState
    object Load : MangaState
    object Error : MangaState
}

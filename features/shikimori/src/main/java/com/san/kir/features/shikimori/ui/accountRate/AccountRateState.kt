package com.san.kir.features.shikimori.ui.accountRate

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.logic.SyncDialogState
import com.san.kir.features.shikimori.logic.useCases.SyncState

internal data class AccountRateState(
    val sync: SyncState,
    val dialog: SyncDialogState,
    val profile: ProfileState,
    val manga: MangaState,
) : ScreenState

internal sealed interface ProfileState {
    data class Ok(val rate: ShikimoriRate) : ProfileState
    object None : ProfileState
    object Load : ProfileState
}

internal sealed interface MangaState {
    data class Ok(val item: ShikimoriManga) : MangaState
    object Load : MangaState
    object Error : MangaState
}

package com.san.kir.features.shikimori.ui.accountRate

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.logic.SyncDialogState
import com.san.kir.features.shikimori.logic.useCases.SyncState

internal data class AccountRateState(
    val sync: SyncState = SyncState.None,
    val dialog: SyncDialogState = SyncDialogState.None,
    val profile: ProfileState = ProfileState.Load,
    val manga: MangaState = MangaState.Load,
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

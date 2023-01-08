package com.san.kir.features.shikimori.ui.localItem

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.logic.SyncDialogState
import com.san.kir.features.shikimori.logic.useCases.SyncState

internal data class LocalItemState(
    val manga: MangaState = MangaState.Load,
    val sync: SyncState = SyncState.None,
    val dialog: SyncDialogState = SyncDialogState.None,
    val profile: ProfileState = ProfileState.Load,
) : ScreenState

internal sealed interface MangaState {
    data class Ok(val item: SimplifiedMangaWithChapterCounts) : MangaState
    data object Load : MangaState
    data object Error : MangaState
}

internal sealed interface ProfileState {
    data class Ok(val rate: ShikimoriRate) : ProfileState
    data object None : ProfileState
    data object Load : ProfileState
}

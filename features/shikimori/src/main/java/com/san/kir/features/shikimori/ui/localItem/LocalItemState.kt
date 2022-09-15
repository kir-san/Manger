package com.san.kir.features.shikimori.ui.localItem

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.logic.SyncDialogState
import com.san.kir.features.shikimori.logic.useCases.SyncState

internal data class LocalItemState(
    val manga: MangaState,
    val sync: SyncState,
    val dialog: SyncDialogState,
    val profile: ProfileState
) : ScreenState

internal sealed interface MangaState {
    data class Ok(val item: SimplifiedMangaWithChapterCounts) : MangaState
    object Load : MangaState
    object Error : MangaState
}

internal sealed interface ProfileState {
    data class Ok(val rate: ShikimoriRate) : ProfileState
    object None : ProfileState
    object Load : ProfileState
}

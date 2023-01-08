package com.san.kir.storage.ui.storages

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.extend.MangaLogo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class StoragesState(
    val items: ImmutableList<Storage> = persistentListOf(),
    val mangas: ImmutableList<MangaLogo?> = persistentListOf(),
    val background: BackgroundState = BackgroundState.Load,
    val size: Double = items.sumOf { it.sizeFull },
    val count: Int = items.count(),
) : ScreenState

internal sealed interface BackgroundState {
    object Load : BackgroundState
    object None : BackgroundState
}

package com.san.kir.storage.ui.storages

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.extend.MangaLogo
import kotlinx.collections.immutable.ImmutableList

data class StoragesState(
    val items: ImmutableList<Storage>,
    val mangas: ImmutableList<MangaLogo?>,
    val background: BackgroundState,
    val size: Double = items.sumOf { it.sizeFull },
    val count: Int = items.count()
) : ScreenState

sealed interface BackgroundState {
    object Load : BackgroundState
    object None : BackgroundState
}

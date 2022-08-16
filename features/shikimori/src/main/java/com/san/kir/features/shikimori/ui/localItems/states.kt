package com.san.kir.features.shikimori.ui.localItems

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.BackgroundTasks
import com.san.kir.features.shikimori.useCases.BindStatus

@Stable
internal data class LocalItemsState(
    val action: BackgroundTasks,
    val unbind: List<BindStatus<SimplifiedMangaWithChapterCounts>>,
) : ScreenState

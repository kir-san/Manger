package com.san.kir.chapters.ui.latest

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.SimplifiedChapter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class LatestState(
    val items: ImmutableList<SelectableItem> = persistentListOf(),
    val hasNewChapters: Boolean = false,
    val hasBackgroundWork: Boolean = true,
    val selectedCount: Int = items.count { it.selected },
    val selectionMode: Boolean = selectedCount > 0,
) : ScreenState {
    override fun toString(): String {
        return "LatestState(items=${items.count()}, hasNewChapters=$hasNewChapters, " +
                "hasBackgroundWork=$hasBackgroundWork, selectionMode=$selectionMode, " +
                "selectedCount=$selectedCount)"
    }
}

@Stable
internal data class SelectableItem(val chapter: SimplifiedChapter, val selected: Boolean)

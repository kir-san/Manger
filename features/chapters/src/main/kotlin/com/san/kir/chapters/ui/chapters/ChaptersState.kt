package com.san.kir.chapters.ui.chapters

import androidx.compose.runtime.Stable
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.extend.SimplifiedChapter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf


internal data class ChaptersState(
    val items: ImmutableList<SelectableItem>,
    val manga: Manga,
    val backgroundAction: Boolean,
    val showTitle: Boolean,
    val nextChapter: NextChapter,
    val chapterFilter: ChapterFilter,
    val selectionCount: Int = items.count { it.selected },
    val selectionMode: Boolean = selectionCount > 0,
    val count: Int = items.count(),
    val readCount: Int = items.count { it.chapter.isRead }
) : ScreenState {
    override fun toString(): String {
        return "ChaptersState(items=${items.count()}, manga=${manga.name}, " +
                "backgroundAction=$backgroundAction, showTitle=$showTitle, " +
                "nextChapter=$nextChapter, chapterFilter=$chapterFilter, count=$count, " +
                "readCount=$readCount, selectionMode=$selectionMode, selectionCount=$selectionCount)"
    }
}

@Stable
internal data class SelectableItem(val chapter: SimplifiedChapter, val selected: Boolean)

internal data class BackgroundActions(
    val updateManga: Boolean = false,
    val updateItems: Boolean = true,
    val updatePages: Boolean = false,
) {
    val result = updateManga || updateItems || updatePages
}

internal data class Items(
    val items: PersistentList<SelectableItem> = persistentListOf(),
    val count: Int = items.count(),
    val readCount: Int = items.count { it.chapter.isRead }
)

@Stable
internal sealed interface NextChapter {

    @Stable
    object None : NextChapter

    @Stable
    data class Ok(val id: Long, val name: String) : NextChapter
}

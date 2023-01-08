package com.san.kir.chapters.ui.chapters

import androidx.compose.runtime.Stable
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.extend.SimplifiedChapter
import com.san.kir.data.models.utils.compareChapterNames
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf


internal data class ChaptersState(
    val items: ImmutableList<SelectableItem> = persistentListOf(),
    val manga: Manga = Manga(),
    val backgroundAction: Boolean = false,
    val showTitle: Boolean = true,
    val nextChapter: NextChapter = NextChapter.None,
    val chapterFilter: ChapterFilter = ChapterFilter.ALL_READ_ASC,
    val selectionCount: Int = items.count { it.selected },
    val selectionMode: Boolean = selectionCount > 0,
    val count: Int = items.count(),
    val readCount: Int = items.count { it.chapter.isRead },
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

internal class SelectableItemComparator : Comparator<SelectableItem> {
    override fun compare(o1: SelectableItem, o2: SelectableItem): Int {
        return compareChapterNames(o1.chapter.name, o2.chapter.name)
    }
}

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
    val readCount: Int = items.count { it.chapter.isRead },
)

@Stable
internal sealed interface NextChapter {
    @Stable
    data object None : NextChapter

    data object Loading : NextChapter

    @Stable
    data class Ok(val id: Long, val name: String) : NextChapter
}

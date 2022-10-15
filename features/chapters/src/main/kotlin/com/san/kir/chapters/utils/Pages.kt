package com.san.kir.chapters.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.san.kir.chapters.R
import com.san.kir.chapters.ui.chapters.ChaptersEvent
import com.san.kir.chapters.ui.chapters.ChaptersState

// Содержимое страницы
internal sealed class ChapterPages(
    val nameId: Int,
    val content: @Composable (
        state: ChaptersState,
        navigateToViewer: (Long) -> Unit,
        sendEvent: (ChaptersEvent) -> Unit
    )
    -> Unit,
)

// Получение списка страниц в зависомости от типа манги
@Composable
internal fun chapterPages(isAlternative: Boolean): List<ChapterPages> {
    return remember(isAlternative) {
        if (isAlternative)
            listOf(AboutPage)
        else
            listOf(AboutPage, ListPage)
    }
}

// Простая страница с минимум возможностей для быстрого продолжения чтения
private object AboutPage : ChapterPages(
    nameId = R.string.list_chapters_page_about,
    content = { state, navigate, _ ->
        AboutPageContent(
            nextChapter = state.nextChapter,
            logo = state.manga.logo,
            readCount = state.readCount,
            count = state.count,
            navigateToViewer = navigate
        )
    }
)

// Страница с списком и инструментами для манипуляций с ним
private object ListPage : ChapterPages(
    nameId = R.string.list_chapters_page_list,
    content = { state, navigate, sendEvent ->
        ListPageContent(
            chapterFilter = state.chapterFilter,
            selectionMode = state.selectionMode,
            items = state.items,
            navigateToViewer = navigate,
            sendEvent = sendEvent,
        )
    }
)

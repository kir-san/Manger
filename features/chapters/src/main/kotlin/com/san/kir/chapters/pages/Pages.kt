package com.san.kir.chapters.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.san.kir.chapters.MainViewModel
import com.san.kir.chapters.R

// Содержимое страницы
internal sealed class ChapterPages(
    val nameId: Int,
    val content: @Composable (MainViewModel) -> Unit,
)

// Получение списка страниц в зависомости от типа манги
internal fun chapterPages(isAlternative: Boolean): List<ChapterPages> {
    return if (isAlternative)
        listOf(AboutPage)
    else
        listOf(AboutPage, ListPage)
}

// Простая страница с минимум возможностей для быстрого продолжения чтения
private object AboutPage : ChapterPages(
    nameId = R.string.list_chapters_page_about,
    content = { viewModel -> AboutPageContent(viewModel) }
)

// Страница с списком и инструментами для манипуляций с ним
private object ListPage : ChapterPages(
    nameId = R.string.list_chapters_page_list,
    content = { viewModel ->
        val manga by viewModel.manga.collectAsState()
        val chapters by viewModel.prepareChapters.collectAsState()
        val filter by viewModel.filter.collectAsState()
        val selectionItems by viewModel.selection.items.collectAsState()
        val selectionMode by viewModel.selection.isEnable.collectAsState()

        ListPageContent(
            manga,
            filter,
            viewModel::updateFilter,
            chapters,
            selectionItems,
            selectionMode,
            viewModel.selection::toggleSelection
        )
    }
)

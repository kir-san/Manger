package com.san.kir.chapters

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.chapters.pages.chapterPages
import com.san.kir.core.compose_utils.Tabs
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalPagerApi::class, ExperimentalCoroutinesApi::class)
@Composable
internal fun ColumnScope.Content(
    action: Boolean,
    viewModel: MainViewModel,
) {
    val isTitle by viewModel.hasTitle.collectAsState(true)
    val manga by viewModel.manga.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    val pagerState = rememberPagerState()
    // Получение списка экранов
    val pages = chapterPages(manga.isAlternativeSite)

    // ПрогрессБар для отображения фоновых операций
    if (action || chapters.isEmpty())
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

    // Заголовки страниц
    if (isTitle)
        Tabs(pagerState, pages.map { it.nameId })

    HorizontalPager(
        count = pages.size,
        state = pagerState,
        modifier = Modifier.weight(1f)
    ) { index ->
        pages[index].content(viewModel)
    }
}

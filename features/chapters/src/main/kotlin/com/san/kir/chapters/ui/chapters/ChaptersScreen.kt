package com.san.kir.chapters.ui.chapters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.chapters.utils.chapterPages
import com.san.kir.chapters.utils.topBar
import com.san.kir.core.compose.ScreenPadding
import com.san.kir.core.compose.Tabs

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChaptersScreen(
    navigateUp: () -> Unit,
    navigateToViewer: (Long) -> Unit,
    mangaId: Long,
) {
    val viewModel: ChaptersViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.sendEvent(ChaptersEvent.Set(mangaId)) }

    val pagerState = rememberPagerState()

    ScreenPadding(
        topBar = topBar(
            selectedCount = state.selectionCount,
            selectionMode = state.selectionMode,
            backgroundAction = state.backgroundAction,
            manga = state.manga,
            navigateUp = navigateUp,
            sendEvent = viewModel::sendEvent
        ),
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            val pages = chapterPages(state.manga.isAlternativeSite)

            if (state.showTitle) Tabs(pagerState, pages.map { it.nameId })

            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f),
                key = { it }
            ) { index ->
                pages[index].content(state, navigateToViewer, viewModel::sendEvent)
            }
        }
    }
}



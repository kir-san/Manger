package com.san.kir.features.shikimori.ui.localItems

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenPadding
import com.san.kir.core.compose_utils.topBar
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.logic.useCases.BindStatus
import com.san.kir.features.shikimori.ui.util.MangaItemContent

@Composable
fun LocalItemsScreen(
    navigateUp: () -> Unit,
    navigateToItem: (Long) -> Unit,
) {
    val viewModel: LocalItemsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenPadding(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.local_items_title),
            subtitle = stringResource(R.string.local_items_subtitle, state.unbind.count()),
            hasAction = state.action.checkBind,
            progressAction = state.action.progress
        ),
        additionalPadding = Dimensions.zero
    ) { contentPadding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(false),
            onRefresh = { viewModel.sendEvent(LocalItemsEvent.Update) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding()
                    )
                    .imePadding()
            ) {
                CatalogContent(state = state.unbind, navigateToItem = navigateToItem)
            }
        }
    }
}

@Composable
private fun CatalogContent(
    state: List<BindStatus<SimplifiedMangaWithChapterCounts>>,
    navigateToItem: (id: Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets
            .systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues()
    ) {
        items(state,
              key = { item -> item.item.id }
        ) { (item, bind) ->
            MangaItemContent(
                avatar = item.logo,
                mangaName = item.name,
                canBind = bind,
                readingChapters = item.read,
                allChapters = item.all,
                onClick = { navigateToItem(item.id) }
            )
        }
    }
}


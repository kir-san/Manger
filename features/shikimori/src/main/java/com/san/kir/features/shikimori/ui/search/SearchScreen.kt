package com.san.kir.features.shikimori.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.topBar
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.logic.useCases.CanBind
import com.san.kir.features.shikimori.ui.util.MangaItemContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShikiSearchScreen(
    navigateUp: () -> Unit,
    navigateToItem: (Long) -> Unit,
    searchText: String,
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.online_search_title),
            initSearchText = searchText,
            onSearchTextChange = { viewModel.sendEvent(SearchEvent.Search(it)) },
            hasAction = state.search is SearchingState.Load,
        ),
        enableCollapsingBars = true
    ) {
        when (val search = state.search) {
            is SearchingState.Ok -> {
                items(search.items, key = { item -> item.id }) { item ->
                    MangaItemContent(
                        avatar = item.image.original,
                        mangaName = item.russian.ifEmpty { item.name ?: "" },
                        readingChapters = 0,
                        secondaryText = stringResource(
                            R.string.volumes_and_chapters,
                            item.volumes ?: 0,
                            item.chapters,
                        ),
                        canBind = CanBind.No,
                        onClick = { navigateToItem(item.id) })
                }
            }
            SearchingState.None -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimensions.default)
                            .animateItemPlacement(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.online_search_nothing),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {}
        }

    }
}

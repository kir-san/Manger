package com.san.kir.catalog.ui.search

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.catalog.R
import com.san.kir.catalog.utils.ListItem
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.topBar
import com.san.kir.data.models.extend.MiniCatalogItem

@Composable
fun SearchScreen(
    navigateUp: () -> Boolean,
    navigateToInfo: (String) -> Unit,
    navigateToAdd: (String) -> Unit,
    searchText: String,
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val query = remember { { arg: String -> viewModel.sendEvent(SearchEvent.Search(arg)) } }
    val update =
        remember { { arg: MiniCatalogItem -> viewModel.sendEvent(SearchEvent.UpdateManga(arg)) } }

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = "${stringResource(R.string.main_menu_search)}: ${state.items.size}",
            initSearchText = searchText,
            onSearchTextChange = query,
            hasAction = state.background,
        ),
        additionalPadding = Dimensions.quarter,
        enableCollapsingBars = true
    ) {
        items(state.items, key = { it.id }) { item ->
            ListItem(
                item, item.catalogName,
                toAdd = navigateToAdd,
                toInfo = navigateToInfo,
                updateItem = update
            )
        }
    }
}

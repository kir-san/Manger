package com.san.kir.manger.ui.application_navigation.catalog.global_search

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.topBar
import com.san.kir.manger.R
import com.san.kir.manger.utils.compose.ListItem

// TODO добавить меню для исключения ненужных каталогов
@Composable
fun GlobalSearchScreen(
    navigateUp: () -> Unit,
    navigateToInfo: (String) -> Unit,
    navigateToAdd: (String) -> Unit,
    searchText: String,
    viewModel: GlobalSearchViewModel = hiltViewModel(),
) {
    val action by viewModel.action.collectAsState()
    val items by viewModel.items.collectAsState()

    ScreenList(
        topBar = topBar(
            navigationListener = navigateUp,
            title = "${stringResource(R.string.main_menu_search)}: ${items.size}",
            initSearchText = searchText,
            onSearchTextChange = viewModel::setSearchText,
            hasAction = action || items.isEmpty(),
            enableSearchField = true,
        ),
        additionalPadding = Dimensions.smaller,
        enableCollapsingBars = true
    ) {
        items(items) { item ->
            ListItem(
                item, item.name, item.catalogName,
                navAddAction = navigateToAdd,
                navInfoAction = navigateToInfo
            )
        }
    }
}





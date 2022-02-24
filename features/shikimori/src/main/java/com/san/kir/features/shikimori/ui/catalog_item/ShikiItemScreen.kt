package com.san.kir.features.shikimori.ui.catalog_item

import androidx.compose.runtime.Composable
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.util.ItemScreen

@Composable
fun ShikiItemScreen(
    viewModel: ShikiItemViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
) {
    ItemScreen(
        viewModel = viewModel,
        navigateUp = navigateUp,
        navigateToSearch = navigateToSearch,
        findTextId = R.string.local_search_searching,
        okTextId = R.string.local_search_sync,
        foundsTextId = R.string.local_search_founds,
        notFoundsTextId = R.string.local_search_not_founds,
        notFoundsSearchTextId = R.string.local_search_not_founds_ex,
    )
}

package com.san.kir.features.shikimori.ui.local_item

import androidx.compose.runtime.Composable
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.util.ItemScreen

@Composable
fun LocalItemScreen(
    viewModel: LocalItemViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
) {

    ItemScreen(
        viewModel = viewModel,
        navigateUp = navigateUp,
        navigateToSearch = navigateToSearch,
        findTextId = R.string.online_search_searching,
        okTextId = R.string.online_search_sync,
        foundsTextId = R.string.online_search_founds,
        notFoundsTextId = R.string.online_search_not_founds,
        notFoundsSearchTextId = R.string.online_search_not_founds_ex,
    )
}




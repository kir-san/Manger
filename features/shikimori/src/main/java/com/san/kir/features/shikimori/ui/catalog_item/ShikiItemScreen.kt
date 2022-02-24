package com.san.kir.features.shikimori.ui.catalog_item

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.util.Dialogs
import com.san.kir.features.shikimori.ui.util.Head
import com.san.kir.features.shikimori.ui.util.body

@Composable
fun ShikiItemScreen(
    viewModel: ShikiItemViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
) {
    val item by viewModel.item.collectAsState()
    val localSearch by viewModel.syncState.collectAsState()
    val dialogState by viewModel.askState.collectAsState()
    val hasAction by viewModel.hasForegroundWork.collectAsState()

    TopBarScreenList(
        navigateUp = navigateUp,
        title = item.manga.russian,
        actions = {
            if (hasAction) {
                CircularProgressIndicator()
            } else {
                MenuIcon(Icons.Default.Update, onClick = viewModel::updateDataFromNetwork)
                if (localSearch is SyncState.Ok) {
                    MenuIcon(Icons.Default.Cancel, onClick = viewModel::askCancelSync)
                }
            }
        }
    ) {
        item {
            Head(
                item.logo,
                item.read,
                item.all,
                item.status,
                item.description,
            )
        }

        item {
            Divider()
        }

        body(
            localSearch,
            findTextId = R.string.local_search_searching,
            okTextId = R.string.local_search_sync,
            foundsTextId = R.string.local_search_founds,
            notFoundsTextId = R.string.local_search_not_founds,
            notFoundsSearchTextId = R.string.local_search_not_founds_ex,
            onListItemClick = viewModel::checkAllChapters,
            onSyncedItemClick = {},
            onSearch = navigateToSearch
        )
    }

    Dialogs(
        dialogState,
        closeDialog = viewModel::askNone,
        checkReadChapters = viewModel::checkReadChapters,
        launchSync = viewModel::launchSync,
        cancelSync = viewModel::cancelSync
    )
}

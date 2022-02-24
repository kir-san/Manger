package com.san.kir.features.shikimori.ui.local_item

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
import com.san.kir.features.shikimori.ui.catalog_item.SyncState
import com.san.kir.features.shikimori.ui.util.Dialogs
import com.san.kir.features.shikimori.ui.util.Head
import com.san.kir.features.shikimori.ui.util.body

@Composable
fun LocalItemScreen(
    viewModel: LocalItemViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
) {
    val item by viewModel.item.collectAsState()
    val localSearch by viewModel.syncState.collectAsState()
    val askState by viewModel.askState.collectAsState()
    val hasAction by viewModel.hasForegroundWork.collectAsState()

    TopBarScreenList(
        navigateUp = navigateUp,
        title = item.name,
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
            findTextId = R.string.online_search_searching,
            okTextId = R.string.online_search_sync,
            foundsTextId = R.string.online_search_founds,
            notFoundsTextId = R.string.online_search_not_founds,
            notFoundsSearchTextId = R.string.online_search_not_founds_ex,
            onListItemClick = viewModel::checkAllChapters,
            onSyncedItemClick = {},
            onSearch = navigateToSearch
        )
    }

    Dialogs(
        askState,
        closeDialog = viewModel::askNone,
        checkReadChapters = viewModel::checkReadChapters,
        launchSync = viewModel::launchSync,
        cancelSync = viewModel::cancelSync
    )
}




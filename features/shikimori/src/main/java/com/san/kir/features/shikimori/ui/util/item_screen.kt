package com.san.kir.features.shikimori.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarEndPadding
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.catalog_item.AskState
import com.san.kir.features.shikimori.ui.catalog_item.CatalogItemViewModel
import com.san.kir.features.shikimori.ui.catalog_item.SyncState
import com.san.kir.features.shikimori.ui.local_item.LocalItemViewModel

@Composable
internal fun ItemScreen(
    viewModel: CatalogItemViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
    findTextId: Int,
    okTextId: Int,
    foundsTextId: Int,
    notFoundsTextId: Int,
    notFoundsSearchTextId: Int,
) {
    val item by viewModel.item.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
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
                if (syncState is SyncState.Ok) {
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
            syncState,
            findTextId = findTextId,
            okTextId = okTextId,
            foundsTextId = foundsTextId,
            notFoundsTextId = notFoundsTextId,
            notFoundsSearchTextId = notFoundsSearchTextId,
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

// Заголовок манги с описанием, данными и аватаркой
@Composable
internal fun Head(
    avatar: String,
    readingChapters: Long,
    allChapters: Long,
    currentStatus: ShikimoriAccount.Status?,
    description: String,
) {
    var showFullDesc by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(systemBarsHorizontalPadding(Dimensions.default)),
    ) {
        Image(
            rememberImage(avatar), contentDescription = "manga avatar",
            modifier = Modifier
                .width(Dimensions.bigImageSize)
                .padding(Dimensions.small),
        )
        Column(modifier = Modifier.weight(1f, true)) {
            Text(stringResource(R.string.reading, readingChapters, allChapters))

            Spacer(modifier = Modifier.padding(Dimensions.smallest))

            StatusText(currentStatus)

            Spacer(modifier = Modifier.padding(Dimensions.smallest))

            Text(
                description,
                maxLines = if (showFullDesc) Int.MAX_VALUE else 4,
                modifier = Modifier.clickable { showFullDesc = !showFullDesc }
            )

            TextButton(
                onClick = { showFullDesc = !showFullDesc },
                contentPadding = PaddingValues(vertical = Dimensions.zero),
                modifier = Modifier.align(Alignment.End)
            ) {
                if (showFullDesc)
                    Text(stringResource(R.string.desc_hide))
                else
                    Text(stringResource(R.string.desc_show))
            }
        }
    }
}

// Отображение соответствующих элементов в зависимости от статуса привязки
internal fun LazyListScope.body(
    localSearch: SyncState,
    findTextId: Int,
    okTextId: Int,
    foundsTextId: Int,
    notFoundsTextId: Int,
    notFoundsSearchTextId: Int,
    onListItemClick: (ShikimoriAccount.AbstractMangaItem) -> Unit,
    onSyncedItemClick: (ShikimoriAccount.AbstractMangaItem) -> Unit,
    onSearch: (String) -> Unit,
) {
    when (localSearch) {
        // Поиск в базе данных, подходящей по названию манги
        SyncState.Find -> item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.default),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text(
                    stringResource(findTextId),
                    modifier = Modifier.padding(start = Dimensions.default),
                )
            }
        }
        // Уже имеется связанная манга
        is SyncState.Ok -> item {
            Column {
                ItemHeader(okTextId)
                MangaItemContent(
                    avatar = localSearch.manga.logo,
                    mangaName = localSearch.manga.name,
                    readingChapters = 0,
                    allChapters = 0,
                    currentStatus = null,
                    isSynced = false,
                    onClick = { onSyncedItemClick(localSearch.manga) })
            }
        }
        // Список подходящей манги
        is SyncState.Founds -> {
            item {
                ItemHeader(foundsTextId)
            }
            items(localSearch.items) { item ->
                MangaItemContent(
                    avatar = item.logo,
                    mangaName = item.name,
                    readingChapters = item.read,
                    allChapters = item.all,
                    currentStatus = item.status,
                    isSynced = false,
                    onClick = { onListItemClick(item) })
            }
        }
        // Поиск ничего не дал
        is SyncState.NotFounds -> item {
            ItemHeader(notFoundsTextId)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(notFoundsSearchTextId))

                Button(onClick = { onSearch(localSearch.name) }) {
                    Text(stringResource(R.string.local_search_not_founds_go))
                }
            }
        }
        SyncState.NoFind -> {
        }
    }
}

// Диалоги появляющиеся в спорных ситуациях
@Composable
internal fun Dialogs(
    askState: AskState,
    closeDialog: () -> Unit,
    checkReadChapters: (ShikimoriAccount.AbstractMangaItem) -> Unit,
    launchSync: (ShikimoriAccount.AbstractMangaItem, Boolean) -> Unit,
    cancelSync: () -> Unit,
) {
    when (askState) {
        AskState.None -> {
        }
        // Разное количество глав
        is AskState.DifferentChapterCount -> {
            AlertDialog(
                onDismissRequest = closeDialog,
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_diffall_text,
                        askState.local,
                        askState.online))
                },
                confirmButton = {
                    TextButton(onClick = { checkReadChapters(askState.manga) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = closeDialog) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }
        // Разное количество прочитанных глав
        is AskState.DifferentReadCount -> {
            AlertDialog(
                onDismissRequest = closeDialog,
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_diffread_text,
                        askState.local,
                        askState.online))
                },
                confirmButton = {
                    TextButton(onClick = { launchSync(askState.manga, false) }) {
                        Text(stringResource(R.string.local_search_dialog_diffread_local))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { launchSync(askState.manga, true) }) {
                        Text(stringResource(R.string.local_search_dialog_diffread_online))
                    }
                }
            )
        }
        // Отмена привязки
        AskState.CancelSync -> {
            AlertDialog(
                onDismissRequest = closeDialog,
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_cancelsync_text))
                },
                confirmButton = {
                    TextButton(onClick = cancelSync) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = closeDialog) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }
    }
}

package com.san.kir.features.shikimori.ui.catalog_item

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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.extend.SimplefiedMangaWithChapterCounts
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.avatar
import com.san.kir.features.shikimori.ui.util.ItemHeader
import com.san.kir.features.shikimori.ui.util.MangaItemContent
import com.san.kir.features.shikimori.ui.util.StatusText

@Composable
fun ShikiItemScreen(
    viewModel: ShikiItemViewModel,
    navigateUp: () -> Unit,
) {
    val item by viewModel.item.collectAsState()
    val localSearch by viewModel.localSearch.collectAsState()
    val dialogState by viewModel.dialog.collectAsState()
    val hasAction by viewModel.hasForegroundWork.collectAsState()

    TopBarScreenList(
        navigateUp = navigateUp,
        title = item.manga.russian,
        actions = {
            if (hasAction) {
                CircularProgressIndicator()
            } else {
                MenuIcon(Icons.Default.Update, onClick = viewModel::updateDataFromNetwork)
                MenuIcon(Icons.Default.Cancel, onClick = viewModel::cancelDialog)
            }
        }
    ) {
        item {
            Head(
                item.manga.avatar,
                item.rate.chapters,
                item.manga.chapters,
                item.rate.status,
                item.manga.description,
            )
        }

        item {
            Divider()
        }

        body(
            localSearch,
            onListItemClick = viewModel::checkAllChapters,
            onSyncedItemClick = {}
        )
    }

    Dialogs(
        dialogState,
        closeDialog = viewModel::dissmissDialog,
        checkReadChapters = viewModel::checkReadChapters,
        launchSync = viewModel::launchSync,
        cancelSync = viewModel::cancelSync
    )
}

@Composable
internal fun Head(
    avatar: String,
    readingChapters: Long,
    allChapters: Long,
    currentStatus: ShikimoriAccount.Status,
    description: String,
) {
    var showFullDesc by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {
        Image(rememberImage(avatar), contentDescription = "manga avatar",
            modifier = Modifier.width(Dimensions.bigImageSize))
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
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                if (showFullDesc)
                    Text(stringResource(R.string.desc_hide))
                else
                    Text(stringResource(R.string.desc_show))
            }
        }
    }
}

internal fun LazyListScope.body(
    localSearch: LocalSearch,
    onListItemClick: (SimplefiedMangaWithChapterCounts) -> Unit,
    onSyncedItemClick: (Manga) -> Unit,
) {
    when (localSearch) {
        // Поиск в базе данных, подходящей по названию манги
        LocalSearch.Searching -> item {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
                Text(stringResource(R.string.local_search_searching))
            }
        }
        // Уже имеется связанная манга
        is LocalSearch.Sync -> item {
            Column {
                ItemHeader(R.string.local_search_sync)
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
        is LocalSearch.Founds -> {
            item {
                ItemHeader(R.string.local_search_founds)
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
        LocalSearch.NotFounds -> item {
            ItemHeader(R.string.local_search_not_founds)


        }
        LocalSearch.NoSearch -> {
        }
    }
}

@Composable
internal fun Dialogs(
    dialogState: Dialog,
    closeDialog: () -> Unit,
    checkReadChapters: (SimplefiedMangaWithChapterCounts) -> Unit,
    launchSync: (SimplefiedMangaWithChapterCounts, Boolean) -> Unit,
    cancelSync: () -> Unit,
) {
    when (dialogState) {
        Dialog.None -> {
        }
        is Dialog.DifferentChapterCount -> {
            AlertDialog(
                onDismissRequest = closeDialog,
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_diffall_text,
                        dialogState.local,
                        dialogState.online))
                },
                confirmButton = {
                    TextButton(onClick = { checkReadChapters(dialogState.manga) }) {
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
        is Dialog.DifferentReadCount -> {
            AlertDialog(
                onDismissRequest = closeDialog,
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_diffread_text,
                        dialogState.local,
                        dialogState.online))
                },
                confirmButton = {
                    TextButton(onClick = { launchSync(dialogState.manga, false) }) {
                        Text(stringResource(R.string.local_search_dialog_diffread_local))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { launchSync(dialogState.manga, true) }) {
                        Text(stringResource(R.string.local_search_dialog_diffread_online))
                    }
                }
            )
        }

        Dialog.CancelSync -> {
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


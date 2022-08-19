package com.san.kir.features.shikimori.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.SmallSpacer
import com.san.kir.core.compose_utils.topBar
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.accountRate.AccountRateEvent
import com.san.kir.features.shikimori.ui.accountRate.SyncState
import com.san.kir.features.shikimori.useCases.CanBind
import com.san.kir.features.shikimori.ui.accountRate.DialogState

@Composable
internal fun ItemScreen(
//    viewModel: CatalogItemViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
    findTextId: Int,
    okTextId: Int,
    foundsTextId: Int,
    notFoundsTextId: Int,
    notFoundsSearchTextId: Int,
) {
    ScreenList(
        topBar = topBar(
            actions = {
            }
        ),
    ) {
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
    onListItemClick: (ShikimoriMangaItem) -> Unit,
    onSyncedItemClick: (ShikimoriMangaItem) -> Unit,
    onSearch: (String) -> Unit,
) {
    when (localSearch) {
        // Поиск в базе данных, подходящей по названию манги
        SyncState.Finding -> item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.default),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(Dimensions.default))
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
                    canBind = CanBind.Already,
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
                    canBind = CanBind.No,
                    onClick = { onListItemClick(item) })
            }
        }
        // Поиск ничего не дал
        is SyncState.NotFounds -> item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ItemHeader(notFoundsTextId)

                SmallSpacer()

                ItemHeader(notFoundsSearchTextId)

                SmallSpacer()

                Button(onClick = { onSearch(localSearch.name) }) {
                    Text(stringResource(R.string.local_search_not_founds_go))
                }
            }
        }
        SyncState.None -> {}
    }
}

// Диалоги появляющиеся в спорных ситуациях
@Composable
internal fun DialogsSyncState(
    state: DialogState,
    onSendEvent: (AccountRateEvent) -> Unit,
) {
    when (state) {
        DialogState.None -> {
        }

        is DialogState.Init -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(AccountRateEvent.DialogDismiss) },
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_bind_items))
                },
                confirmButton = {
                    TextButton(onClick = { onSendEvent(AccountRateEvent.SyncNext(state.manga)) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onSendEvent(AccountRateEvent.DialogDismiss) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }

        // Разное количество глав
        is DialogState.DifferentChapterCount -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(AccountRateEvent.DialogDismiss) },
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(
                        stringResource(
                            R.string.local_search_dialog_diffall_text,
                            state.local,
                            state.online
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onSendEvent(AccountRateEvent.SyncNext(state.manga)) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onSendEvent(AccountRateEvent.DialogDismiss) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }
        // Разное количество прочитанных глав
        is DialogState.DifferentReadCount -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(AccountRateEvent.DialogDismiss) },
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(
                        stringResource(
                            R.string.local_search_dialog_diffread_text,
                            state.local,
                            state.online
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSendEvent(AccountRateEvent.SyncNext(state.manga, false))
                        }
                    ) {
                        Text(stringResource(R.string.local_search_dialog_diffread_local))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onSendEvent(AccountRateEvent.SyncNext(state.manga, true))
                        }
                    ) {
                        Text(stringResource(R.string.local_search_dialog_diffread_online))
                    }
                }
            )
        }
        // Отмена привязки
        DialogState.CancelSync -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(AccountRateEvent.DialogDismiss) },
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_cancelsync_text))
                },
                confirmButton = {
                    TextButton(onClick = { onSendEvent(AccountRateEvent.SyncCancel) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onSendEvent(AccountRateEvent.DialogDismiss) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }
    }
}

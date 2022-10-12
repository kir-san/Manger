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
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.SmallSpacer
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.logic.SyncDialogEvent
import com.san.kir.features.shikimori.logic.SyncDialogState
import com.san.kir.features.shikimori.logic.useCases.CanBind
import com.san.kir.features.shikimori.logic.useCases.SyncState

// Отображение соответствующих элементов в зависимости от статуса привязки
internal fun LazyListScope.body(
    state: SyncState,
    findTextId: Int,
    okTextId: Int,
    foundsTextId: Int,
    notFoundsTextId: Int,
    notFoundsSearchTextId: Int,
    onSendEvent: (SyncDialogEvent) -> Unit,
    onSearch: (String) -> Unit,
) {
    when (state) {
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
                    avatar = state.manga.logo,
                    mangaName = state.manga.name,
                    readingChapters = state.manga.read,
                    allChapters = state.manga.all,
                    currentStatus = state.manga.status,
                    canBind = CanBind.Already,
                    onClick = { onSendEvent(SyncDialogEvent.SyncToggle(state.manga)) })
            }
        }
        // Список подходящей манги
        is SyncState.Founds -> {
            item {
                ItemHeader(foundsTextId)
            }
            items(state.items) { item ->
                MangaItemContent(
                    avatar = item.logo,
                    mangaName = item.name,
                    readingChapters = item.read,
                    allChapters = item.all,
                    currentStatus = item.status,
                    canBind = CanBind.No,
                    onClick = { onSendEvent(SyncDialogEvent.SyncToggle(item)) })
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

                Button(onClick = { onSearch(state.name) }) {
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
    state: SyncDialogState,
    onSendEvent: (SyncDialogEvent) -> Unit,
) {
    when (state) {
        SyncDialogState.None -> {
        }

        is SyncDialogState.Init -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(SyncDialogEvent.DialogDismiss) },
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_bind_items))
                },
                confirmButton = {
                    TextButton(onClick = { onSendEvent(SyncDialogEvent.SyncNext()) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onSendEvent(SyncDialogEvent.DialogDismiss) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }

        // Разное количество глав
        is SyncDialogState.DifferentChapterCount -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(SyncDialogEvent.DialogDismiss) },
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
                    TextButton(onClick = { onSendEvent(SyncDialogEvent.SyncNext()) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onSendEvent(SyncDialogEvent.DialogDismiss) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }
        // Разное количество прочитанных глав
        is SyncDialogState.DifferentReadCount -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(SyncDialogEvent.DialogDismiss) },
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
                            onSendEvent(SyncDialogEvent.SyncNext(false))
                        }
                    ) {
                        Text(stringResource(R.string.local_search_dialog_diffread_local))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onSendEvent(SyncDialogEvent.SyncNext(true))
                        }
                    ) {
                        Text(stringResource(R.string.local_search_dialog_diffread_online))
                    }
                }
            )
        }
        // Отмена привязки
        is SyncDialogState.CancelSync -> {
            AlertDialog(
                onDismissRequest = { onSendEvent(SyncDialogEvent.DialogDismiss) },
                title = {
                    Text(stringResource(R.string.local_search_dialog_diff_title))
                },
                text = {
                    Text(stringResource(R.string.local_search_dialog_cancelsync_text))
                },
                confirmButton = {
                    TextButton(onClick = { onSendEvent(SyncDialogEvent.SyncCancel(state.rate)) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onSendEvent(SyncDialogEvent.DialogDismiss) }) {
                        Text(stringResource(R.string.local_search_dialog_diffall_no))
                    }
                }
            )
        }
    }
}

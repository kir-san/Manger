package com.san.kir.chapters.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import com.san.kir.chapters.R
import com.san.kir.chapters.ui.chapters.ChaptersEvent
import com.san.kir.chapters.ui.chapters.Selection
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.TopBarActions
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.animation.FromTopToTopAnimContent
import com.san.kir.core.compose.topBar
import com.san.kir.data.models.base.Manga

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun topBar(
    selectedCount: Int,
    selectionMode: Boolean,
    backgroundAction: Boolean,
    manga: Manga,
    navigateUp: () -> Boolean,
    sendEvent: (ChaptersEvent) -> Unit,
) = topBar(
    titleContent = {
        FromTopToTopAnimContent(targetState = selectionMode) {
            if (it) {
                Text(
                    pluralStringResource(
                        R.plurals.list_chapters_action_selected, selectedCount, selectedCount
                    ),
                    maxLines = 1
                )
            } else {
                Text(manga.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    },
    hasAction = backgroundAction,
    navigationButton = if (selectionMode) {
        NavigationButton.Close { sendEvent(ChaptersEvent.WithSelected(Selection.Clear)) }
    } else {
        NavigationButton.Back(navigateUp)
    },
    backgroundColor = if (selectionMode) {
        MaterialTheme.colors.secondary
    } else {
        MaterialTheme.colors.primarySurface
    },
    actions = {
        FromEndToEndAnimContent(targetState = selectionMode) {
            Row {
                if (it) {
                    SelectionModeActions(selectedCount) {
                        sendEvent(ChaptersEvent.WithSelected(it))
                    }
                } else {
                    DefaultModeActions(manga.isUpdate, manga.isAlternativeSort, sendEvent)
                }
            }
        }
    }
)

@Composable
private fun TopBarActions.DefaultModeActions(
    isUpdate: Boolean,
    isAlternativeSort: Boolean,
    sendEvent: (ChaptersEvent) -> Unit,
) {
    BottomAnimatedVisibility(isUpdate) {
        MenuIcon(Icons.Default.Update) { sendEvent(ChaptersEvent.UpdateManga) }
    }

    ExpandedMenu {
        /* Быстрая загрузка глав */
        MenuText(R.string.list_chapters_download_next) { sendEvent(ChaptersEvent.DownloadNext) }
        MenuText(R.string.list_chapters_download_not_read) { sendEvent(ChaptersEvent.DownloadNotRead) }
        MenuText(R.string.list_chapters_download_all) { sendEvent(ChaptersEvent.DownloadAll) }

        // настройки обновления и сортировки индивидуальные для каждой манги
        CheckedMenuText(R.string.list_chapters_is_update, isUpdate) {
            sendEvent(ChaptersEvent.ChangeIsUpdate)
        }
        CheckedMenuText(R.string.list_chapters_change_sort, isAlternativeSort) {
            sendEvent(ChaptersEvent.ChangeMangaSort)
        }
    }
}

@Composable
private fun TopBarActions.SelectionModeActions(
    selectedCount: Int,
    sendEvent: (Selection) -> Unit,
) {
    val state = remember { ActionsState() }

    // Выделение всех элементов
    MenuIcon(Icons.Default.SelectAll) { sendEvent(Selection.All) }

    // Загрузка выделеных элементов
    MenuIcon(Icons.Default.Download) { sendEvent(Selection.Download) }

    ExpandedMenu {
        // удаление из хранилища
        MenuText(R.string.action_delete, onClick = state::showDeleteDialog)

        // смена статуса чтения
        MenuText(R.string.action_set_read) { sendEvent(Selection.SetRead(true)) }
        MenuText(R.string.action_set_not_read) { sendEvent(Selection.SetRead(false)) }

        // Обновление страниц у выделеных глав
        MenuText(R.string.action_update_pages) { sendEvent(Selection.UpdatePages) }

        // Расширенное выделение элементов: выше и ниже единственно выделеного
        if (selectedCount == 1) {
            MenuText(R.string.action_select_above) { sendEvent(Selection.Above) }
            MenuText(R.string.action_select_below) { sendEvent(Selection.Below) }
        }

        // Удаление элементов из базы данных, может помощь при дубликатах
        MenuText(R.string.action_full_delete, onClick = state::showFullDeleteDialog)
    }

    DeleteSelectedChaptersAlertDialog(
        visible = state.showDeleteDialog,
        onClose = state::closeDeleteDialog,
        onClick = { sendEvent(Selection.DeleteFiles) },
    )

    FullDeleteChaptersAlertDialog(
        visible = state.showFullDeleteDialog,
        onClose = state::closeFullDeleteDialog,
        onClick = { sendEvent(Selection.DeleteFromDB) }
    )
}

// Управление состоянием UI элементов
private class ActionsState {
    var showDeleteDialog by mutableStateOf(false)
        private set
    var showFullDeleteDialog by mutableStateOf(false)
        private set

    fun showDeleteDialog() {
        showDeleteDialog = true
    }

    fun closeDeleteDialog() {
        showDeleteDialog = false
    }

    fun showFullDeleteDialog() {
        showFullDeleteDialog = true
    }

    fun closeFullDeleteDialog() {
        showFullDeleteDialog = false
    }
}

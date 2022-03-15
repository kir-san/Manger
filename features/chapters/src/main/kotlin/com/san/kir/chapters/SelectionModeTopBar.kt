package com.san.kir.chapters

import android.content.Context
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.san.kir.chapters.pages.DeleteSelectedChaptersAlertDialog
import com.san.kir.chapters.pages.FullDeleteChaptersAlertDialog
import com.san.kir.core.compose_utils.TopBarActions
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.utils.quantitySimple

// AppBar для режима выделения
@Composable
internal fun selectionModeTopBar(
    viewModel: MainViewModel,
    selectionItems: List<Boolean>,
    changeAction: (Boolean) -> Unit,
    context: Context = LocalContext.current,
) = topBar(
    title = context.quantitySimple(
        R.plurals.list_chapters_action_selected,
        selectionItems.count { it }
    ),
    actions = selectionModeActions(viewModel, changeAction),
    navigationListener = viewModel.selection::clear,
    backgroundColor = MaterialTheme.colors.secondary,
)

// Пункты меню
private fun selectionModeActions(
    viewModel: MainViewModel,
    changeAction: (Boolean) -> Unit,
): @Composable TopBarActions.() -> Unit = {

    val selectionItems by viewModel.selection.items.collectAsState()

    val state = remember { ActionsState() }

    // Выделение всех элементов
    MenuIcon(
        icon = Icons.Default.SelectAll,
        onClick = viewModel.selection::fullFill,
    )

    // Загрузка выделеных элементов
    MenuIcon(
        icon = Icons.Default.Download,
        onClick = viewModel::downloadSelectedItems,
    )

    ExpandedMenu {
        // удаление из хранилища
        MenuText(
            id = R.string.action_delete,
            onClick = state::showDeleteDialog
        )

        // смена статуса чтения
        MenuText(id = R.string.action_set_read) {
            changeAction(true)
            viewModel.setReadStatus(true).invokeOnCompletion {
                changeAction(false)
            }
        }
        MenuText(id = R.string.action_set_not_read) {
            changeAction(true)
            viewModel.setReadStatus(false).invokeOnCompletion {
                changeAction(false)
            }
        }

        // Обновление страниц у выделеных глав
        MenuText(id = R.string.action_update_pages) {
            changeAction(true)
            viewModel.updatePagesForSelectedItems().invokeOnCompletion {
                changeAction(false)
            }
        }

        // Расширенное выделение элементов: выше и ниже единственно выделеного
        if (selectionItems.count { it } == 1) {
            MenuText(
                id = R.string.action_select_above,
                onClick = viewModel.selection::aboveFill
            )

            MenuText(
                id = R.string.action_select_below,
                onClick = viewModel.selection::belowFill
            )
        }

        // Удаление элементов из базы данных, может помощь при дубликатах
        MenuText(
            id = R.string.action_full_delete,
            onClick = state::showFullDeleteDialog
        )
    }

    DeleteSelectedChaptersAlertDialog(
        visible = state.showDeleteDialog,
        onClose = state::closeDeleteDialog,
        onClick = viewModel::deleteSelectedItems,
    )

    FullDeleteChaptersAlertDialog(
        visible = state.showFullDeleteDialog,
        onClose = state::closeFullDeleteDialog,
        onClick = {
            changeAction(true)
            viewModel.fullDeleteSelectedItems().invokeOnCompletion {
                changeAction(false)
            }
        },
    )
}


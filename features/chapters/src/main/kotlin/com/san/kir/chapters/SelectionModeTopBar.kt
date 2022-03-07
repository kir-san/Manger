package com.san.kir.chapters

import android.content.Context
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import com.san.kir.chapters.pages.DeleteSelectedChaptersAlertDialog
import com.san.kir.chapters.pages.FullDeleteChaptersAlertDialog
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.MenuText
import com.san.kir.core.utils.quantitySimple

// AppBar для режима выделения
@Composable
internal fun SelectionModeTopBar(
    viewModel: MainViewModel,
    changeAction: (Boolean) -> Unit,
    context: Context = LocalContext.current,
) {
    val selectionItems by viewModel.selection.items.collectAsState()

    TopAppBar(
        title = {
            Text(
                context.quantitySimple(
                    R.plurals.list_chapters_action_selected,
                    selectionItems.count { it }
                )
            )
        },

        navigationIcon = {
            IconButton(onClick = viewModel.selection::clear) {
                Icon(Icons.Default.ArrowBack, "")
            }
        },

        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(Dimensions.zero),

        actions = selectionModeActions(viewModel, changeAction),

        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false, applyTop = false
        ),

        backgroundColor = MaterialTheme.colors.secondary
    )
}

// Пункты меню
private fun selectionModeActions(
    viewModel: MainViewModel,
    changeAction: (Boolean) -> Unit,
): @Composable RowScope.() -> Unit = {

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

    // Расскрытие дополнительного меню
    MenuIcon(
        icon = Icons.Default.MoreVert,
        onClick = state::expandMenu,
    )

    DropdownMenu(
        expanded = state.showExpandMenu,
        onDismissRequest = state::collapseMenu,
    ) {
        // удаление из хранилища
        MenuText(id = R.string.action_delete) {
            state.collapseMenu()
            state.showDeleteDialog()
        }

        // смена статуса чтения
        MenuText(id = R.string.action_set_read) {
            state.collapseMenu()
            changeAction(true)
            viewModel.setReadStatus(true).invokeOnCompletion {
                changeAction(false)
            }
        }
        MenuText(id = R.string.action_set_not_read) {
            state.collapseMenu()
            changeAction(true)
            viewModel.setReadStatus(false).invokeOnCompletion {
                changeAction(false)
            }
        }

        // Обновление страниц у выделеных глав
        MenuText(id = R.string.action_update_pages) {
            state.collapseMenu()
            changeAction(true)
            viewModel.updatePagesForSelectedItems().invokeOnCompletion {
                changeAction(false)
            }
        }

        // Расширенное выделение элементов: выше и ниже единственно выделеного
        if (selectionItems.count { it } == 1) {
            MenuText(id = R.string.action_select_above) {
                state.collapseMenu()
                viewModel.selection.aboveFill()
            }

            MenuText(id = R.string.action_select_below) {
                state.collapseMenu()
                viewModel.selection.belowFill()
            }
        }

        // Удаление элементов из базы данных, может помощь при дубликатах
        MenuText(id = R.string.action_full_delete) {
            state.collapseMenu()
            state.showFullDeleteDialog()
        }
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


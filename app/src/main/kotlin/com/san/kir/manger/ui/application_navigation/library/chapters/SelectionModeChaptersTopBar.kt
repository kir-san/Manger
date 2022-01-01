package com.san.kir.manger.ui.application_navigation.library.chapters

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import com.san.kir.manger.R
import com.san.kir.ui.utils.MenuIcon
import com.san.kir.ui.utils.MenuText
import com.san.kir.core.utils.quantitySimple

@Composable
fun SelectionModeChaptersTopBar(
    viewModel: ChaptersViewModel,
    changeAction: (Boolean) -> Unit,
    context: Context = LocalContext.current,
) {

    TopAppBar(
        title = {
            Text(
                context.quantitySimple(
                    R.plurals.list_chapters_action_selected, viewModel.selectedItems.count { it }
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = { viewModel.removeSelection() }) {
                Icon(Icons.Default.ArrowBack, "")
            }
        },
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(0.dp),
        actions = {
            Actions(viewModel, changeAction)
        },
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false, applyTop = false
        ),
        backgroundColor = MaterialTheme.colors.secondary
    )
}

@Composable
private fun Actions(
    viewModel: ChaptersViewModel,
    changeAction: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var fullDeleteDialog by remember { mutableStateOf(false) }

    MenuIcon(icon = Icons.Default.SelectAll) {
        viewModel.selectAllItems()
    }

    MenuIcon(icon = Icons.Default.Download) {
        viewModel.downloadSelectedItems()
    }

    MenuIcon(icon = Icons.Default.MoreVert) {
        expanded = true
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        // удаление из хранилища
        MenuText(id = R.string.action_delete) {
            expanded = false
            deleteDialog = true
        }

        // смена статуса чтения
        MenuText(id = R.string.action_set_read) {
            expanded = false
            changeAction(true)
            viewModel.setReadStatus(true).invokeOnCompletion {
                changeAction(false)
            }
        }
        MenuText(id = R.string.action_set_not_read) {
            expanded = false
            changeAction(true)
            viewModel.setReadStatus(false).invokeOnCompletion {
                changeAction(false)
            }
        }

        MenuText(id = R.string.action_update_pages) {
            expanded = false
            changeAction(true)
            viewModel.updatePagesForSelectedItems().invokeOnCompletion {
                changeAction(false)
            }
        }

        if (viewModel.selectedItems.count { it } == 1) {
            MenuText(id = R.string.action_select_above) {
                expanded = false
                viewModel.selectAboveItems()
            }

            MenuText(id = R.string.action_select_below) {
                expanded = false
                viewModel.selectBelowItems()
            }
        }


        MenuText(id = R.string.action_full_delete) {
            expanded = false
            fullDeleteDialog = true
        }
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text(stringResource(R.string.list_chapters_remove_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelectedItems()
                    deleteDialog = false
                }) {
                    Text(
                        stringResource(R.string.list_chapters_remove_yes)
                            .toUpperCase(Locale.current)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) {
                    Text(
                        stringResource(R.string.list_chapters_remove_no)
                            .toUpperCase(Locale.current)
                    )
                }
            }
        )
    }

    if (fullDeleteDialog) {
        AlertDialog(
            onDismissRequest = { fullDeleteDialog = false },
            title = { Text(stringResource(R.string.action_full_delete_title)) },
            text = { Text(stringResource(R.string.action_full_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    changeAction(true)
                    viewModel.fullDeleteSelectedItems().invokeOnCompletion {
                        changeAction(false)
                    }
                    fullDeleteDialog = false
                }) {
                    Text(
                        stringResource(R.string.list_chapters_remove_yes)
                            .toUpperCase(Locale.current)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { fullDeleteDialog = false }) {
                    Text(
                        stringResource(R.string.list_chapters_remove_no)
                            .toUpperCase(Locale.current)
                    )
                }
            }
        )
    }
}

package com.san.kir.library.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MenuDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.animation.BottomAnimatedVisibility
import com.san.kir.core.compose_utils.horizontalInsetsPadding
import com.san.kir.core.compose_utils.systemBarBottomPadding
import com.san.kir.data.models.extend.SimplifiedManga
import com.san.kir.library.R
import com.san.kir.library.ui.library.ItemsState
import com.san.kir.library.ui.library.LibraryEvent
import com.san.kir.library.ui.library.SelectedMangaState
import kotlinx.collections.immutable.ImmutableMap

@Composable
internal fun LibraryDropUpMenu(
    navigateToInfo: (String) -> Unit,
    navigateToStorage: (Long) -> Unit,
    navigateToStats: (Long) -> Unit,
    itemsState: ItemsState,
    selectedManga: SelectedMangaState.Visible,
    sendEvent: (LibraryEvent) -> Unit
) {
    var deleteDialog by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(systemBarBottomPadding())
    ) {

        Title(text = selectedManga.item.name, sendEvent = sendEvent)

        Properties {
            sendEvent(LibraryEvent.NonSelect)
            navigateToInfo(selectedManga.item.name)
        }

        CategoryChanger(
            changerVisibility = expandedCategory,
            itemsState = itemsState,
            selectedManga = selectedManga.item,
            onClick = {
                expandedCategory = !expandedCategory
                deleteDialog = false
            },
            onItemClick = { categoryId ->
                expandedCategory = false
                sendEvent(LibraryEvent.ChangeCategory(categoryId))
            }
        )

        Storage {
            sendEvent(LibraryEvent.NonSelect)
            navigateToStorage(selectedManga.item.id)
        }

        Statistics {
            sendEvent(LibraryEvent.NonSelect)
            navigateToStats(selectedManga.item.id)
        }

        Delete(
            changerVisibility = deleteDialog,
            onClick = {
                deleteDialog = !deleteDialog
                expandedCategory = false
            },
            onDismiss = {
                deleteDialog = false
                it?.let { sendEvent(LibraryEvent.DeleteManga(selectedManga.item.id, it)) }
            },
        )
    }
}

@Composable
private inline fun Title(
    text: String,
    crossinline sendEvent: (LibraryEvent) -> Unit,
) {
    DropdownMenuItem(
        onClick = { sendEvent(LibraryEvent.NonSelect) },
        modifier = Modifier
            .background(color = MaterialTheme.colors.primary)
            .horizontalInsetsPadding()
    ) {

        Text(
            stringResource(R.string.library_popupmenu_title, text),
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
private fun Properties(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = Modifier
            .horizontalInsetsPadding()
    ) {
        Text(stringResource(R.string.library_popupmenu_about))
    }
}

@Composable
private fun ColumnScope.CategoryChanger(
    changerVisibility: Boolean,
    itemsState: ItemsState,
    selectedManga: SimplifiedManga,
    onClick: () -> Unit,
    onItemClick: (Long) -> Unit,
) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = Modifier
            .horizontalInsetsPadding()
    ) {
        Text(
            stringResource(id = R.string.library_popupmenu_set_category),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = selectedManga.category,
            style = MaterialTheme.typography.subtitle2
        )
    }

    if (itemsState is ItemsState.Ok)
        ExpandedCategories(
            visibility = changerVisibility,
            categories = itemsState.categories.remove(selectedManga.categoryId),
            onItemChanged = onItemClick
        )
}

@Composable
private fun ColumnScope.ExpandedCategories(
    visibility: Boolean,
    categories: ImmutableMap<Long, String>,
    onItemChanged: (Long) -> Unit,
) {

    BottomAnimatedVisibility(
        visible = visibility,
        modifier = Modifier.background(Color(0xFF525252))
    ) {
        Column {
            categories.forEach { (key, value) ->
                DropdownMenuItem(
                    onClick = { onItemChanged(key) },
                    modifier = Modifier
                        .horizontalInsetsPadding()
                ) {
                    Text(text = value, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun Storage(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = Modifier
            .horizontalInsetsPadding()
    ) {
        Text(stringResource(R.string.library_popupmenu_storage))
    }
}

@Composable
private fun Statistics(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = Modifier
            .horizontalInsetsPadding()
    ) {
        Text(stringResource(R.string.library_popupmenu_statistic))
    }
}


@Composable
fun Delete(
    changerVisibility: Boolean,
    onClick: () -> Unit,
    onDismiss: (Boolean?) -> Unit,
) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = Modifier
            .horizontalInsetsPadding()
    ) {
        Text(stringResource(R.string.library_popupmenu_delete))
    }

    BottomAnimatedVisibility (
        visible = changerVisibility,
        modifier = Modifier.background(Color(0xFF525252))
    ) {

        Column(
            modifier = Modifier
                .horizontalInsetsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = Dimensions.Items.height)
                    .padding(MenuDefaults.DropdownMenuItemContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.library_popupmenu_delete_message),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val pad = PaddingValues(4.dp, 4.dp)

                OutlinedButton(
                    onClick = { onDismiss(true) },
                    modifier = Modifier.padding(pad)
                ) {
                    Text(text = stringResource(id = R.string.library_popupmenu_delete_ok_with_files))
                }

                OutlinedButton(
                    onClick = { onDismiss(false) },
                    modifier = Modifier.padding(pad)
                ) {
                    Text(text = stringResource(id = R.string.library_popupmenu_delete_ok))
                }

                OutlinedButton(
                    onClick = { onDismiss(null) },
                    modifier = Modifier.padding(pad)
                ) {
                    Text(text = stringResource(id = R.string.library_popupmenu_delete_no))
                }
            }
        }
    }
}

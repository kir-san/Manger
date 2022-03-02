package com.san.kir.manger.ui.application_navigation.storage.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuText
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.core.utils.formatDouble
import com.san.kir.data.models.base.Storage
import com.san.kir.manger.R
import com.san.kir.manger.ui.application_navigation.storage.StorageNavTarget
import com.san.kir.manger.utils.compose.StorageProgressBar
import com.san.kir.manger.utils.compose.navigate
import kotlin.math.roundToInt

@Composable
fun StorageScreen(
    nav: NavHostController,
    viewModel: StorageViewModel = hiltViewModel(),
) {
    val viewState by viewModel.state.collectAsState()
    val allStorage = viewModel.allStorage.collectAsLazyPagingItems()

    TopBarScreenList(
        navigateUp = nav::navigateUp,
        title = stringResource(R.string.main_menu_storage) + " " +
                if (viewState.storageSize > 0) {
                    stringResource(
                        R.string.main_menu_storage_size_mb,
                        formatDouble(viewState.storageSize)
                    )
                } else "",
        subtitle = LocalContext.current.resources.getQuantityString(
            R.plurals.storage_subtitle,
            viewState.storageCounts,
            viewState.storageCounts
        ),
        additionalPadding = Dimensions.smallest
    ) {
        items(items = allStorage) { item ->
            item?.let { ItemView(nav, item, viewModel) }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ItemView(
    nav: NavHostController,
    item: Storage,
    viewModel: StorageViewModel,
) {
    val viewState by viewModel.state.collectAsState()
    val manga by viewModel.mangaFromPath(item.path).collectAsState(null)

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isExists = manga != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                manga?.let { nav.navigate(StorageNavTarget.Storage, it.name) }
                    ?: run { showMenu = true }
            }
            .padding(vertical = Dimensions.smallest, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding())
    ) {
        // Иконка манги, если для этой папки она еще есть
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            Image(
                rememberImage(url = manga?.logo),
                contentDescription = "",
                modifier = Modifier
                    .padding(3.dp)
                    .clip(CircleShape)
                    .size(60.dp),
                contentScale = ContentScale.Crop
            )
            if (!isExists) {
                Text(
                    text = stringResource(id = R.string.storage_not_in_bd),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, true)
                .align(Alignment.CenterVertically)
                .padding(Dimensions.smallest),
            verticalArrangement = Arrangement.Center
        ) {
            // Название папки с мангой
            Text(text = item.name, maxLines = 1)

            // Текстовая Информация о занимаемом месте
            Text(
                stringResource(
                    R.string.storage_manga_item_size_text,
                    formatDouble(item.sizeFull),
                    if (viewState.storageSize != 0.0) {
                        (item.sizeFull / viewState.storageSize * 100).roundToInt()
                    } else {
                        0
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                style = MaterialTheme.typography.subtitle1
            )

            // Прогрессбар занимаемого места
            StorageProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .height(10.dp),
                max = viewState.storageSize,
                full = item.sizeFull,
                read = item.sizeRead,
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuText(id = R.string.storage_item_menu_full_delete) {
                showDeleteDialog = true
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            text = { Text(text = stringResource(id = R.string.storage_item_alert_message)) },
            confirmButton = {
                OutlinedButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(item)
                }) {
                    Text(text = stringResource(id = R.string.storage_item_alert_positive))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.storage_item_alert_negative))
                }

            }
        )
    }
}

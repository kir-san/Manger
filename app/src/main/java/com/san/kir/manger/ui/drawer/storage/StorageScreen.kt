package com.san.kir.manger.ui.drawer.storage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.ui.Drawer
import com.san.kir.manger.ui.MainViewModel
import com.san.kir.manger.ui.StorageManga
import com.san.kir.manger.ui.utils.MenuText
import com.san.kir.manger.ui.utils.StorageProgressBar
import com.san.kir.manger.ui.utils.navigate
import com.san.kir.manger.ui.utils.navigationBarsPadding
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.view_models.TitleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun StorageScreen(
    mainNav: NavHostController,
    vm: TitleViewModel = hiltViewModel(mainNav.getBackStackEntry(Drawer.route))
) {
    val viewModel: StorageViewModel = viewModel()
    val viewState by viewModel.state.collectAsState()

    vm.setTitle(
        title = stringResource(id = R.string.main_menu_storage) +
                if (viewState.storageSize > 0) {
                    stringResource(
                        id = R.string.main_menu_storage_size_mb,
                        formatDouble(viewState.storageSize)
                    )
                } else "",
        subtitle = LocalContext.current.resources.getQuantityString(
            R.plurals.storage_subtitle,
            viewState.storageCounts,
            viewState.storageCounts
        )
    )



    LazyColumn(modifier = Modifier.navigationBarsPadding()) {
        items(items = viewState.items, key = { storage -> storage.id }) { item ->
            ItemView(item, mainNav)
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun ItemView(item: Storage, mainNav: NavHostController) {
    val viewModel: StorageViewModel = viewModel()
    val viewState by viewModel.state.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var logo by remember { mutableStateOf(ImageBitmap(60, 60)) }
    var isExists by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel
                    .mangaFromPath(item.path)
                    ?.let {
                        mainNav.navigate(StorageManga, it)
                    } ?: run {
                    showMenu = true
                }
            }
    ) {
        // Иконка манги, если для этой папки она еще есть
        Image(
            logo,
            contentDescription = "",
            modifier = Modifier
                .padding(3.dp)
                .clip(CircleShape)
                .size(60.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .align(Alignment.CenterVertically)
                .padding(5.dp)
        ) {
            // Название папки с мангой
            Text(text = item.name, maxLines = 1)
            // Текстовая Информация о занимаемом месте
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.storage_manga_item_size_text,
                        formatDouble(item.sizeFull),
                        if (viewState.storageSize != 0.0) {
                            (item.sizeFull / viewState.storageSize * 100).roundToInt()
                        } else {
                            0
                        }
                    ), modifier = Modifier.weight(1f, true),
                    style = MaterialTheme.typography.subtitle1
                )
                AnimatedVisibility(visible = isExists) {
                    Text(
                        text = stringResource(id = R.string.storage_not_in_bd),
//                        style = MaterialTheme.typography.h3
                    )
                }
            }
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

    LaunchedEffect(key1 = item) {
        val manga = withContext(Dispatchers.Default) { viewModel.mangaFromPath(item.path) }
        isExists = manga != null

        manga?.let { m ->
            loadImage(m.logo) {
                onSuccess { image ->
                    logo = image
                }
                start()
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

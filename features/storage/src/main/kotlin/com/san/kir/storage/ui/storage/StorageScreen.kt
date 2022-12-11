package com.san.kir.storage.ui.storage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.animation.VectorConverter
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.format
import com.san.kir.data.models.base.Storage
import com.san.kir.storage.R
import com.san.kir.storage.utils.StorageProgressBar
import kotlinx.coroutines.launch

@Composable
fun StorageScreen(
    navigateUp: () -> Boolean,
    mangaId: Long,
    hasUpdate: Boolean
) {
    val viewModel: StorageViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.sendEvent(StorageEvent.Set(mangaId, hasUpdate)) }

    ScreenContent(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = state.mangaName,
            hasAction = state.background !is BackgroundState.None,
        ),
    ) {
        Content(
            background = state.background,
            storage = state.item,
            size = state.size,
            sendEvent = viewModel::sendEvent
        )
    }
}

@Composable
private fun Content(
    background: BackgroundState,
    storage: Storage,
    size: Double,
    sendEvent: (StorageEvent) -> Unit,
) {
    var dialog by remember { mutableStateOf<DeleteStatus>(DeleteStatus.None) }

    val all = remember { Animatable(0.0, Double.VectorConverter) }
    val full = remember { Animatable(0.0, Double.VectorConverter) }
    val read = remember { Animatable(0.0, Double.VectorConverter) }

    LaunchedEffect(size) { all.animateTo(size, TweenSpec(2500, 0, LinearEasing)) }
    LaunchedEffect(storage) {
        launch { full.animateTo(storage.sizeFull, TweenSpec(2500, 300, LinearEasing)) }
        launch { read.animateTo(storage.sizeRead, TweenSpec(2500, 600, LinearEasing)) }
    }


    StorageProgressBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.ProgressBar.storage)
            .padding(vertical = Dimensions.half)
            .horizontalInsetsPadding(),
        max = all.value,
        full = full.value,
        read = read.value
    )

    // Строка с отображением всего занятого места
    StorageItem(Color.LightGray, R.string.storage_item_all_size, all.value.format())
    // Строка с отображением занятого места выбранной манги
    StorageItem(Color(0xFFFF4081), R.string.storage_item_manga_size, full.value.format())
    // Строка с отображение занятого места прочитанных глав выбранной манги
    StorageItem(Color(0xFF222e7a), R.string.storage_item_read_size, read.value.format())

    // Кнопки очистки от манги появляющиеся только если есть, что удалять
    when (background) {
        BackgroundState.Load -> {}
        BackgroundState.None -> {
            // Удаление прочитанных глав
            AnimatedVisibility(storage.sizeRead > 0.0) {
                DeleteItem(R.string.library_popupmenu_delete_read_chapters) {
                    dialog = DeleteStatus.All
                }
            }
            // Удаление содержимого папки
            AnimatedVisibility(storage.sizeFull > 0.0) {
                DeleteItem(R.string.library_popupmenu_delete_all) {
                    dialog = DeleteStatus.Read
                }
            }
        }

        BackgroundState.Deleting -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.library_popupmenu_delete_read_chapters_delete),
                    modifier = Modifier.padding(Dimensions.default)
                )
            }
        }
    }

    // Окна подтверждиения операций удаления
    if (dialog != DeleteStatus.None)
        AlertDialog(
            onDismissRequest = { dialog = DeleteStatus.None },
            text = { Text(stringResource(R.string.library_popupmenu_delete_read_chapters_message)) },
            confirmButton = {
                DialogBtn(R.string.library_popupmenu_delete_read_chapters_ok) {
                    when (dialog) {
                        DeleteStatus.All -> sendEvent(StorageEvent.DeleteAll)
                        DeleteStatus.Read -> sendEvent(StorageEvent.DeleteRead)
                        else -> {}
                    }
                    dialog = DeleteStatus.None
                }
            },
            dismissButton = {
                DialogBtn(R.string.library_popupmenu_delete_read_chapters_no) {
                    dialog = DeleteStatus.None
                }
            }
        )
}

@Composable
private fun StorageItem(color: Color, id: Int, vararg formatArgs: Any) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.half),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = Dimensions.default)
                .horizontalInsetsPadding()
                .size(Dimensions.Image.storage)
                .background(color = color, shape = RoundedCornerShape(3))
        )
        Text(stringResource(id, *formatArgs))
    }
}

@Composable
private fun DeleteItem(id: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = Dimensions.half),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier
                .padding(end = Dimensions.default)
                .horizontalInsetsPadding()
                .size(Dimensions.Image.storage)
        )
        Text(stringResource(id))
    }
}

@Composable
private fun DialogBtn(id: Int, onClick: () -> Unit) {
    OutlinedButton(onClick) {
        Text(stringResource(id))
    }
}

sealed class DeleteStatus {
    object Read : DeleteStatus()
    object All : DeleteStatus()
    object None : DeleteStatus()
}

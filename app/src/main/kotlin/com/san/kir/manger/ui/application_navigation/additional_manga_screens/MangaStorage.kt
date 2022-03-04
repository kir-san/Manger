package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import com.san.kir.manger.R
import com.san.kir.manger.utils.compose.StorageProgressBar
import com.san.kir.manger.utils.extensions.format
import com.san.kir.background.works.ChapterDeleteWorker
import com.san.kir.core.compose_utils.TopBarScreenContent

@Composable
fun MangaStorageScreen(
    navigateUp: () -> Unit,
    viewModel: MangaStorageViewModel,
) {
    val manga by viewModel.manga.collectAsState()

    TopBarScreenContent(
        navigateUp = navigateUp,
        title = manga.name,
    ) {
        MangaStorageContent(viewModel)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MangaStorageContent(
    viewModel: MangaStorageViewModel,
    ctx: Context = LocalContext.current,
) {

    val generalSize by viewModel.generalSize.collectAsState(0.0)
    val storageItem by viewModel.storage.collectAsState()

    var action by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<DeleteStatus>(DeleteStatus.None) }

    StorageProgressBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 7.dp),
        max = generalSize,
        full = storageItem.sizeFull,
        read = storageItem.sizeRead
    )

    // Строка с отображением всего занятого места
    StorageItem(Color.LightGray, R.string.storage_item_all_size, generalSize.format())
    // Строка с отображением занятого места выбранной манги
    StorageItem(Color(0xFFFF4081), R.string.storage_item_manga_size, storageItem.sizeFull.format())
    // Строка с отображение зянятого места прочитанных глав выбранной манги
    StorageItem(Color(0xFF222e7a), R.string.storage_item_read_size, storageItem.sizeRead.format())

    // Кнопки очистки от манги появляющиеся только если есть, что удалять
    if (action.not()) {
        // Удаление прочитанных глав
        AnimatedVisibility(visible = storageItem.sizeRead > 0.0) {
            DeleteItem(id = R.string.library_popupmenu_delete_read_chapters) {
                dialog = DeleteStatus.All
            }
        }
        // Удаление содержимого папки
        AnimatedVisibility(visible = storageItem.sizeFull > 0.0) {
            DeleteItem(id = R.string.library_popupmenu_delete_all) {
                dialog = DeleteStatus.Read
            }
        }
    }

    AnimatedVisibility(visible = action) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator()
            Text(
                text = stringResource(id = R.string.library_popupmenu_delete_read_chapters_delete),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Окна подтверждиения операций удаления
    if (dialog != DeleteStatus.None)
        AlertDialog(
            onDismissRequest = { dialog = DeleteStatus.None },
            text = {
                Text(text = stringResource(id = R.string.library_popupmenu_delete_read_chapters_message))
            },
            confirmButton = {
                DialogBtn(id = R.string.library_popupmenu_delete_read_chapters_ok) {
                    action = true
                    viewModel.deleteChapters(dialog)
                    dialog = DeleteStatus.None
                }
            },
            dismissButton = {
                DialogBtn(id = R.string.library_popupmenu_delete_read_chapters_no) {
                    dialog = DeleteStatus.None
                }
            }
        )

    LaunchedEffect(action) {
        WorkManager.getInstance(ctx)
            .getWorkInfosByTagLiveData(ChapterDeleteWorker.tag)
            .asFlow()
            .collect { works ->
                if (works.isNotEmpty()) {
                    action = works.all { it.state.isFinished }.not()
                }
            }
    }
}

private val Modifier.sizeAndPadding: Modifier
    get() {
        return this
            .size(52.dp, 30.dp)
            .padding(end = 16.dp)
    }
private val Modifier.maxWidthAndPadding: Modifier
    get() {
        return this
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    }

@Composable
private fun StorageItem(color: Color, id: Int, vararg formatArgs: Any) {
    Row(
        modifier = Modifier.maxWidthAndPadding,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .sizeAndPadding
                .background(color = color, shape = RoundedCornerShape(3))
        )
        Text(text = stringResource(id = id, *formatArgs))
    }
}

@Composable
private fun DeleteItem(id: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .maxWidthAndPadding
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.sizeAndPadding
        )
        Text(text = stringResource(id = id))
    }
}

@Composable
private fun DialogBtn(id: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Text(text = stringResource(id = id))
    }
}

sealed class DeleteStatus {
    object Read : DeleteStatus()
    object All : DeleteStatus()
    object None : DeleteStatus()
}

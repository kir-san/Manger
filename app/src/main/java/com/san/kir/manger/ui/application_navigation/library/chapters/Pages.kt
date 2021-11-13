package com.san.kir.manger.ui.application_navigation.library.chapters

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import com.google.accompanist.insets.navigationBarsPadding
import com.san.kir.ankofork.startActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.viewer.ViewerActivity
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.workmanager.ChapterDeleteWorker
import com.san.kir.manger.workmanager.ReadChapterDelete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

sealed class ChapterPages(
    val nameId: Int,
    val content: @Composable (ChaptersViewModel) -> Unit,
)

fun chapterPages(isAlternative: Boolean): List<ChapterPages> {
    return if (isAlternative)
        listOf(AboutPage)
    else
        listOf(AboutPage, ListPage)
}

object AboutPage : ChapterPages(
    nameId = R.string.list_chapters_page_about,
    content = { viewModel -> AboutPageContent(viewModel) }
)

object ListPage : ChapterPages(
    nameId = R.string.list_chapters_page_list,
    content = { viewModel ->
        ListPageContent(
            viewModel.manga,
            viewModel.filter,
            { viewModel.filter = it },
            viewModel.prepareChapters,
            viewModel.selectedItems,
            viewModel.selectionMode,
            viewModel::onSelectItem
        )
    }
)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AboutPageContent(
    viewModel: ChaptersViewModel,
    context: Context = LocalContext.current,
) {
    val fullChaptersCount = viewModel.chapters.count()
    val readChaptersCount = viewModel.chapters.count { it.isRead }

    var logoManga by remember { mutableStateOf(ImageBitmap(60, 60)) }

    // для кнопки продолжить чтение
    var isSearch by rememberSaveable { mutableStateOf(true) }
    var isContinue by rememberSaveable { mutableStateOf(false) }
    var messageContinue by rememberSaveable {
        mutableStateOf(context.getString(R.string.list_chapters_about_search_continue))
    }

    var deleteDialog by remember { mutableStateOf(false) }
    var progressDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Image(
            logoManga,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // информация о прочитанных главах
            Text(
                stringResource(
                    R.string.list_chapters_about_read,
                    readChaptersCount,
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.chapters,
                        readChaptersCount
                    ),
                    fullChaptersCount
                ),
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(18.dp)
                    .fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // удаления прочитанных глав
                Button(
                    onClick = { deleteDialog = true }, modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.list_chapters_about_delete))
                }
                // Чтение манги с начала
                Button(
                    onClick = {
                        context.startActivity<ViewerActivity>(
                            "manga" to viewModel.manga,
                            "is" to viewModel.manga.isAlternativeSort
                        )
                    }, modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.list_chapters_about_start))
                }

            }
            // Продолжение чтения
            Button(
                onClick = {
                    context.startActivity<ViewerActivity>(
                        "manga" to viewModel.manga,
                        "is" to viewModel.manga.isAlternativeSort,
                        "continue" to true
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = isContinue,
            ) {
                if (isSearch) CircularProgressIndicator()

                Text(messageContinue)
            }
        }
    }

    if (deleteDialog) {
        DeleteChaptersDialog({ progressDeleteDialog = true }, { deleteDialog = false })
    }

    if (progressDeleteDialog) {
        ProgressDeletingChaptersDialog(viewModel.manga) { progressDeleteDialog = false }
    }

    LaunchedEffect(viewModel.manga) {
        loadImage(viewModel.manga.logo, context) {
            onSuccess { image ->
                logoManga = image
            }
            start()
        }

        withContext(Dispatchers.Default) {
            viewModel.getFirstNotReadChapters(viewModel.manga)?.let { ch ->
                isContinue = true
                messageContinue = context.getString(R.string.list_chapters_about_continue, ch.name)
            } ?: kotlin.run {
                isContinue = false
                messageContinue = context.getString(R.string.list_chapters_about_not_continue)
            }
            isSearch = false

        }
    }
}

@Composable
private fun DeleteChaptersDialog(
    additionalAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = {
            Text(stringResource(R.string.library_popupmenu_delete_read_chapters_message))
        },
        confirmButton = {
            TextButton(onClick = {
                additionalAction()
                onDismiss()
            }) {
                Text(
                    stringResource(R.string.library_popupmenu_delete_read_chapters_ok)
                        .toUpperCase(Locale.current)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(R.string.library_popupmenu_delete_read_chapters_no)
                        .toUpperCase(Locale.current)
                )
            }
        }
    )
}

@Composable
private fun ProgressDeletingChaptersDialog(
    manga: Manga,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    var progress by remember { mutableStateOf(true) }
    var message by remember {
        mutableStateOf(R.string.library_popupmenu_delete_read_chapters_delete)
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        buttons = {
            if (!progress)
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.library_popupmenu_delete_read_chapters_btn_close))
                }
        },
        text = {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (progress) CircularProgressIndicator()
                Text(
                    stringResource(message),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }
    )

    LaunchedEffect(true) {
        ChapterDeleteWorker.addTask<ReadChapterDelete>(ctx, manga)

        WorkManager
            .getInstance(ctx)
            .getWorkInfosByTagLiveData(ChapterDeleteWorker.tag)
            .asFlow()
            .collect { works ->
                if (works.isNotEmpty() && works.all { it.state.isFinished }) {
                    progress = false
                    message = R.string.library_popupmenu_delete_read_chapters_ready
                }
            }
    }
}

package com.san.kir.chapters

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import com.san.kir.background.util.collectWorkInfoByTag
import com.san.kir.background.works.ChapterDeleteWorker
import com.san.kir.background.works.ReadChapterDelete
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.data.models.base.Manga
import com.san.kir.features.viewer.MangaViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val fullChaptersCount = viewModel.chapters.count()
    val readChaptersCount = viewModel.chapters.count { it.isRead }
    val firstNotReadChapter by viewModel.getFirstNotReadChapters().collectAsState(null)

    // для кнопки продолжить чтение
    val messageContinue = remember(firstNotReadChapter) {
        firstNotReadChapter?.let { ch ->
            context.getString(R.string.list_chapters_about_continue, ch.name)
        } ?: run {
            context.getString(R.string.list_chapters_about_not_continue)
        }
    }

    var deleteDialog by remember { mutableStateOf(false) }
    var progressDeleteDialog by remember { mutableStateOf(false) }
    BoxWithConstraints() {

    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            rememberImage(url = viewModel.manga.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
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
                    .padding(systemBarsHorizontalPadding(all = Dimensions.default))
                    .fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )

            // Продолжение чтения
            Button(
                onClick = {
                    scope.launch {
                        val chapter = viewModel.getFirstNotReadChapters().first()
                        chapter?.let {
                            MangaViewer.start(context, it.id)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.default)
                    .navigationBarsPadding(start = false, end = false)
                    .systemBarsPadding(top = false, bottom = false),
                enabled = firstNotReadChapter != null,
            ) {
                if (firstNotReadChapter == null) CircularProgressIndicator()

                Text(messageContinue.toUpperCase(Locale.current),
                    modifier = Modifier.padding(Dimensions.default),
                    textAlign = TextAlign.Center)
            }
        }
    }

    if (deleteDialog) {
        DeleteChaptersDialog({ progressDeleteDialog = true }, { deleteDialog = false })
    }

    if (progressDeleteDialog) {
        ProgressDeletingChaptersDialog(viewModel.manga) { progressDeleteDialog = false }
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

        ctx.collectWorkInfoByTag(ChapterDeleteWorker.tag) { works ->
            if (works.isNotEmpty() && works.all { it.state.isFinished }) {
                progress = false
                message = R.string.library_popupmenu_delete_read_chapters_ready
            }
        }
    }
}

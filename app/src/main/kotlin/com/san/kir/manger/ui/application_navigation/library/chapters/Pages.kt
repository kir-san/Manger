package com.san.kir.manger.ui.application_navigation.library.chapters

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import com.google.accompanist.insets.navigationBarsPadding
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.data.models.base.Manga
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.workmanager.ChapterDeleteWorker
import com.san.kir.manger.foreground_work.workmanager.ReadChapterDelete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
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

private object Refs {
    const val logo = "logo"
    const val continueBtn = "continueBtn"
    const val startBtn = "startBtn"
    const val deleteBtn = "deleteBtn"
    const val info = "info"
}

private val defaultMargin = 16.dp

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

    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
        constraintSet = decoupledConstraintSet(),
    ) {
        Image(
            rememberImage(url = viewModel.manga.logo),
            modifier = Modifier.layoutId(Refs.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

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
                .layoutId(Refs.info),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )

        // удаления прочитанных глав
        Button(
            onClick = { deleteDialog = true },
            modifier = Modifier
                .layoutId(Refs.deleteBtn)
                .padding(horizontal = defaultMargin)
        ) {
            Text(text = stringResource(id = R.string.list_chapters_about_delete),
                modifier = Modifier.padding(5.dp),
                textAlign = TextAlign.Center)
        }

        // Чтение манги с начала
        Button(
            onClick = {
                scope.launch {
                    val chapter = viewModel.firstChapter()
                    MangaViewer.start(context, chapter.id)
                }

            },
            modifier = Modifier
                .layoutId(Refs.startBtn)
                .padding(end = defaultMargin)
        ) {
            Text(text = stringResource(id = R.string.list_chapters_about_start),
                modifier = Modifier.padding(5.dp),
                textAlign = TextAlign.Center)
        }

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
                .navigationBarsPadding(start = false, end = false)
                .layoutId(Refs.continueBtn),
            enabled = firstNotReadChapter != null,
        ) {
            if (firstNotReadChapter == null) CircularProgressIndicator()

            Text(messageContinue.toUpperCase(Locale.current),
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center)
        }
    }

    if (deleteDialog) {
        DeleteChaptersDialog({ progressDeleteDialog = true }, { deleteDialog = false })
    }

    if (progressDeleteDialog) {
        ProgressDeletingChaptersDialog(viewModel.manga) { progressDeleteDialog = false }
    }
}

private fun decoupledConstraintSet(): ConstraintSet {
    return ConstraintSet {
        val logo = createRefFor(Refs.logo)
        val continueBtn = createRefFor(Refs.continueBtn)
        val startBtn = createRefFor(Refs.startBtn)
        val deleteBtn = createRefFor(Refs.deleteBtn)
        val info = createRefFor(Refs.info)

        constrain(logo) {
            centerTo(parent)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        }

        constrain(continueBtn) {
            linkTo(parent.start, parent.end, defaultMargin, defaultMargin)
            bottom.linkTo(parent.bottom, defaultMargin)
            width = Dimension.fillToConstraints
        }

        constrain(deleteBtn) {
            start.linkTo(parent.start)
            bottom.linkTo(continueBtn.top, defaultMargin)
            width = Dimension.fillToConstraints
        }

        constrain(startBtn) {
            linkTo(deleteBtn.top, deleteBtn.bottom)
            linkTo(deleteBtn.end, parent.end)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        }

        constrain(info) {
            linkTo(parent.start, parent.end)
            bottom.linkTo(startBtn.top, defaultMargin)
            width = Dimension.fillToConstraints
        }

        createHorizontalChain(deleteBtn, startBtn)
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

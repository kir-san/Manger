package com.san.kir.chapters.ui.latest

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.chapters.R
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.SmallerSpacer
import com.san.kir.core.compose.TopBarActions
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.toast
import com.san.kir.data.models.extend.SimplifiedChapter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LatestScreen(
    navigateUp: () -> Unit,
    navigateToViewer: (Long) -> Unit,
) {
    val viewModel: LatestViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenList(
        topBar = topBar(
            title =
            if (state.selectionMode) {
                pluralStringResource(
                    R.plurals.list_chapters_action_selected,
                    state.selectedCount, state.selectedCount
                )
            } else {
                stringResource(R.string.main_menu_latest_count, state.items.size)
            },
            actions = latestActions(
                selectionMode = state.selectionMode,
                hasNewChapters = state.hasNewChapters,
                sendEvent = viewModel::sendEvent
            ),
            hasAction = state.hasBackgroundWork,
            navigationButton = navigationButton(
                selectionMode = state.selectionMode,
                navigateUp = navigateUp,
                sendEvent = viewModel::sendEvent
            )
        ),
        additionalPadding = Dimensions.zero,
        enableCollapsingBars = true,
    ) {
        itemsIndexed(state.items, key = { _, item -> item.chapter.id }) { index, item ->
            LatestItemContent(
                item = item,
                index = index,
                selectionMode = state.selectionMode,
                navigateToViewer = navigateToViewer,
                sendEvent = viewModel::sendEvent
            )
        }
    }
}

@Composable
private fun navigationButton(
    selectionMode: Boolean,
    navigateUp: () -> Unit,
    sendEvent: (LatestEvent) -> Unit
): NavigationButton {
    return if (selectionMode) {
        NavigationButton.Close { sendEvent(LatestEvent.UnselectAll) }
    } else {
        NavigationButton.Back(navigateUp)
    }
}

private fun latestActions(
    selectionMode: Boolean,
    hasNewChapters: Boolean,
    sendEvent: (LatestEvent) -> Unit
): @Composable TopBarActions.() -> Unit = {
    if (selectionMode) {
        MenuIcon(Icons.Default.Delete) { sendEvent(LatestEvent.RemoveSelected) }
    } else {
        ExpandedMenu {
            if (hasNewChapters) {
                MenuText(R.string.latest_chapter_download_new) { sendEvent(LatestEvent.DownloadNew) }
            }

            MenuText(R.string.latest_chapter_clean) { sendEvent(LatestEvent.CleanAll) }
            MenuText(R.string.latest_chapter_clean_read) { sendEvent(LatestEvent.CleanRead) }
            MenuText(R.string.latest_chapter_clean_download) { sendEvent(LatestEvent.CleanDownloaded) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyItemScope.LatestItemContent(
    item: SelectableItem,
    index: Int,
    selectionMode: Boolean,
    navigateToViewer: (Long) -> Unit,
    sendEvent: (LatestEvent) -> Unit,
) {
    val context: Context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                when {
                    item.selected -> Color(0x9934b5e4)
                    item.chapter.isRead -> Color(0xffa5a2a2)
                    else -> Color.Transparent
                }
            )
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick(
                    context = context,
                    selectionMode = selectionMode,
                    chapter = item.chapter,
                    navigateToViewer = navigateToViewer,
                    sendEvent = { sendEvent(LatestEvent.ChangeSelect(index)) }
                ),
                onLongClick = { sendEvent(LatestEvent.ChangeSelect(index)) }
            )
            .padding(vertical = Dimensions.small, horizontal = Dimensions.default)
            .horizontalInsetsPadding()
            .animateItemPlacement(),
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ChapterName(item.chapter.name)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.smaller),
            ) {
                MangaName(item.chapter.manga)

                StatusText(
                    state = item.chapter.status,
                    progress = item.chapter.progress,
                    date = item.chapter.date
                )
            }
        }

        DownloadButton(
            state = item.chapter.status,
            itemId = item.chapter.id,
            sendEvent = sendEvent
        )
    }
}

@Composable
private fun ChapterName(name: String) {
    Text(
        name,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.MangaName(manga: String) {
    Text(
        manga,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.StatusText(state: DownloadState, progress: Int, date: String) {
    FromBottomToBottomAnimContent(targetState = state) {
        when (it) {
            DownloadState.LOADING -> {
                Indicator()
                Text(
                    stringResource(R.string.list_chapters_download_progress, progress),
                    style = MaterialTheme.typography.body2,
                )
            }

            DownloadState.QUEUED -> {
                Indicator()
                Text(
                    stringResource(R.string.list_chapters_queue),
                    style = MaterialTheme.typography.body2,
                )

                SmallerSpacer()
            }

            DownloadState.PAUSED,
            DownloadState.COMPLETED,
            DownloadState.UNKNOWN -> {
                Text(
                    date,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
    }
}

@Composable
private fun RowScope.Indicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(Dimensions.bigger),
        strokeWidth = ProgressIndicatorDefaults.StrokeWidth - 1.dp
    )

    SmallerSpacer()
}

@Composable
private fun DownloadButton(state: DownloadState, itemId: Long, sendEvent: (LatestEvent) -> Unit) {
    FromEndToEndAnimContent(targetState = state) {
        when (it) {
            DownloadState.LOADING,
            DownloadState.QUEUED ->
                // cancel button
                IconButton(onClick = { sendEvent(LatestEvent.StopDownload(itemId)) }) {
                    Icon(Icons.Default.Close, contentDescription = "cancel download button")
                }

            DownloadState.PAUSED,
            DownloadState.COMPLETED,
            DownloadState.UNKNOWN ->
                // download button
                IconButton(onClick = { sendEvent(LatestEvent.StartDownload(itemId)) }) {
                    Icon(Icons.Default.Download, contentDescription = "download button")
                }
        }
    }
}

private fun onClick(
    context: Context,
    selectionMode: Boolean,
    chapter: SimplifiedChapter,
    navigateToViewer: (Long) -> Unit,
    sendEvent: () -> Unit,
): () -> Unit {
    return {
        if (selectionMode.not()) {
            when (chapter.status) {
                DownloadState.QUEUED,
                DownloadState.LOADING ->
                    context.toast(R.string.list_chapters_open_is_download)

                else ->
                    if (chapter.pages.isEmpty() || chapter.pages.any { it.isBlank() }) {
                        context.longToast(R.string.list_chapters_open_not_exists)
                    } else {
                        navigateToViewer(chapter.id)
                    }

            }
        } else {
            sendEvent()
        }
    }
}

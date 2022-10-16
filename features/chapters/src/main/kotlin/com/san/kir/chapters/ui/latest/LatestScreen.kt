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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.chapters.R
import com.san.kir.chapters.utils.ChapterDate
import com.san.kir.chapters.utils.ChapterName
import com.san.kir.chapters.utils.Download
import com.san.kir.chapters.utils.DownloadButton
import com.san.kir.chapters.utils.LoadingIndicator
import com.san.kir.chapters.utils.LoadingText
import com.san.kir.chapters.utils.WaitingText
import com.san.kir.chapters.utils.onClickItem
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.SmallerSpacer
import com.san.kir.core.compose.TopBarActions
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.animation.FromTopToTopAnimContent
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.support.DownloadState

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
            titleContent = {
                FromTopToTopAnimContent(targetState = state.selectionMode) {
                    if (it) {
                        Text(
                            pluralStringResource(
                                R.plurals.list_chapters_action_selected,
                                state.selectedCount, state.selectedCount
                            )
                        )
                    } else {
                        Text(stringResource(R.string.main_menu_latest_count, state.items.size))
                    }
                }
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
    FromEndToEndAnimContent(targetState = selectionMode) {
        when (it) {
            true -> Row {
                MenuIcon(Icons.Default.Delete) { sendEvent(LatestEvent.RemoveSelected) }
                MenuIcon(Icons.Default.Download) { sendEvent(LatestEvent.DownloadSelected) }
            }

            false ->
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
                onClick = onClickItem(
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
                    progress = item.chapter.downloadProgress,
                    date = item.chapter.date
                )
            }
        }


        DownloadButton(item.chapter.status) {
            when (it) {
                Download.START -> sendEvent(LatestEvent.StartDownload(item.chapter.id))
                Download.STOP -> sendEvent(LatestEvent.StopDownload(item.chapter.id))
            }
        }
    }
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
private fun StatusText(state: DownloadState, progress: Int, date: String) {
    FromBottomToBottomAnimContent(targetState = state) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (it) {
                DownloadState.LOADING -> {
                    LoadingIndicator()
                    LoadingText(progress)
                }

                DownloadState.QUEUED -> {
                    LoadingIndicator()
                    WaitingText()
                    SmallerSpacer()
                }

                DownloadState.PAUSED,
                DownloadState.COMPLETED,
                DownloadState.UNKNOWN -> {
                    ChapterDate(date)
                }
            }
        }
    }
}




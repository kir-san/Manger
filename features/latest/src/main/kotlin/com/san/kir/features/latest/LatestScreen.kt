package com.san.kir.features.latest

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.download.DownloadService
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.base.Chapter
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.quantitySimple
import com.san.kir.core.utils.toast
import com.san.kir.features.latest.work.LatestClearWorkers
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.MenuText
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun LatestScreen(
    navigateUp: () -> Unit,
    navigateToViewer: (Chapter) -> Unit,
    viewModel: LatestViewModel,
) {
    var isAction by remember { mutableStateOf(false) }

    TopBarScreenList(
        additionalPadding = 0.dp,
        navigateUp = navigateUp,
        title = if (viewModel.selectionMode) {
            LocalContext.current.quantitySimple(
                R.plurals.list_chapters_action_selected, viewModel.selectedItems.count { it }
            )
        } else {
            stringResource(R.string.main_menu_latest_count, viewModel.allItems.size)
        },
        actions = { LatestActions(viewModel) }
    ) {
        item {
            if (isAction) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        itemsIndexed(viewModel.allItems) { index, chapter ->
            LatestItemContent(
                chapter = chapter,
                isSelected = viewModel.selectedItems[index],
                index = index,
                viewModel = viewModel,
                navigateToViewer = navigateToViewer
            )
        }
    }

    val context = LocalContext.current

    LaunchedEffect("collect") {
        WorkManager
            .getInstance(context)
            .getWorkInfosByTagLiveData(LatestClearWorkers.tag)
            .asFlow()
            .filter { works -> works.isNotEmpty() }
            .map { works -> !works.all { it.state.isFinished } }
            .collect { isAction = it }
    }
}

@Composable
internal fun LatestActions(
    viewModel: LatestViewModel,
    context: Context = LocalContext.current,
) {
    var expanded by remember { mutableStateOf(false) }

    if (viewModel.selectionMode) {
        MenuIcon(icon = Icons.Default.Delete) {
            viewModel.deleteSelectedItems()
        }
    } else {
        MenuIcon(icon = Icons.Default.MoreVert) {
            expanded = true
        }
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

        if (viewModel.hasNewChapters) {
            MenuText(R.string.latest_chapter_download_new) {
                expanded = false
                viewModel.downloadNewChapters()
            }
        }

        MenuText(R.string.latest_chapter_clean) {
            expanded = false
            LatestClearWorkers.clearAll(context)
        }

        MenuText(R.string.latest_chapter_clean_read) {
            expanded = false
            LatestClearWorkers.clearReaded(context)
        }

        MenuText(R.string.latest_chapter_clean_download) {
            expanded = false
            LatestClearWorkers.clearDownloaded(context)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun LatestItemContent(
    navigateToViewer: (Chapter) -> Unit,
    chapter: Chapter,
    isSelected: Boolean,
    index: Int,
    viewModel: LatestViewModel,
    context: Context = LocalContext.current,
) {
    val downloadIndicator by remember(chapter) {
        mutableStateOf(
            chapter.status == DownloadState.QUEUED
                    || chapter.status == DownloadState.LOADING
        )
    }
    val queueIndicator by remember(chapter) {
        mutableStateOf(chapter.status == DownloadState.QUEUED)
    }
    val loadingIndicator by remember(chapter) {
        mutableStateOf(chapter.status == DownloadState.LOADING)
    }
    val downloadPercent by remember(chapter) {
        mutableStateOf(
            if (chapter.totalPages == 0) 0
            else chapter.downloadPages * 100 / chapter.totalPages
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                when {
                    isSelected -> Color(0x9934b5e4)
                    chapter.isRead -> Color(0xffa5a2a2)
                    else -> Color.Transparent
                }
            )
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (viewModel.selectionMode.not()) {
                        if (downloadIndicator) {
                            context.toast(R.string.list_chapters_open_is_download)
                        } else {
                            if (chapter.pages.isNullOrEmpty() || chapter.pages.any { it.isBlank() }) {
                                context.longToast(R.string.list_chapters_open_not_exists)
                            } else {
                                navigateToViewer(chapter)
                            }
                        }
                    } else viewModel.onSelectItem(index)
                },
                onLongClick = {
                    viewModel.onSelectItem(index)
                }
            )
            .padding(vertical = Dimensions.smaller, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding()),
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // name
            Text(
                chapter.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp),
            ) {
                Text(
                    chapter.manga,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )

                // downloadIndicator
                AnimatedVisibility(downloadIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(19.dp),
                        strokeWidth = ProgressIndicatorDefaults.StrokeWidth - 1.dp
                    )

                    Spacer(modifier = Modifier.width(5.dp))
                }

                AnimatedVisibility(queueIndicator) {
                    Text(
                        stringResource(R.string.list_chapters_queue),
                        style = MaterialTheme.typography.body2,
                    )

                    Spacer(modifier = Modifier.width(5.dp))
                }

                // Date
                AnimatedVisibility(downloadIndicator.not()) {
                    Text(
                        chapter.date,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.alignByBaseline(),
                    )
                }

                AnimatedVisibility(loadingIndicator) {
                    Text(
                        stringResource(R.string.list_chapters_download_progress, downloadPercent),
                        style = MaterialTheme.typography.body2,
                    )
                }
            }
        }

        // download button
        AnimatedVisibility(downloadIndicator.not()) {
            IconButton(
                onClick = {
                    DownloadService.start(context, chapter)
                },
            ) {
                Icon(Icons.Default.Download, contentDescription = "download button")
            }
        }

        // cancel button
        AnimatedVisibility(downloadIndicator) {
            IconButton(
                onClick = {
                    DownloadService.pause(context, chapter)
                },
            ) {
                Icon(Icons.Default.Close, contentDescription = "cancel download button")
            }
        }
    }
}

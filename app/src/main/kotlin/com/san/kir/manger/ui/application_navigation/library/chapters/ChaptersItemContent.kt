package com.san.kir.manger.ui.application_navigation.library.chapters

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.toast
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.countPages
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.manger.R

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChaptersItemContent(
    manga: Manga,
    chapter: Chapter,
    isSelected: Boolean,
    selectionMode: Boolean,
    onSelectItem: () -> Unit,
    context: Context = LocalContext.current,
) {
    val countPagesInMemory by produceState(initialValue = 0, chapter, manga) {
        withDefaultContext {
            value = chapter.countPages
        }
    }

    val deleteIndicator = countPagesInMemory > 0

    val downloadIndicator =
        chapter.status == DownloadState.QUEUED || chapter.status == DownloadState.LOADING

    val queueIndicator = chapter.status == DownloadState.QUEUED
    val loadingIndicator = chapter.status == DownloadState.LOADING
    val downloadPercent =
        if (chapter.totalPages == 0) 0
        else chapter.downloadPages * 100 / chapter.totalPages

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
                    if (selectionMode.not()) {
                        if (downloadIndicator) {
                            context.toast(R.string.list_chapters_open_is_download)
                        } else {
                            if (chapter.pages.isNullOrEmpty() || chapter.pages.any { it.isBlank() }) {
                                context.longToast(R.string.list_chapters_open_not_exists)
                            } else {
                                MangaViewer.start(context, chapter.id)
                            }
                        }
                    } else onSelectItem()
                },
                onLongClick = {
                    onSelectItem()
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(10.dp)
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
                // downloadIndicator
                AnimatedVisibility(downloadIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(19.dp)
                            .padding(end = 5.dp),
                        strokeWidth = ProgressIndicatorDefaults.StrokeWidth - 1.dp
                    )
                }

                // status
                AnimatedVisibility(downloadIndicator.not()) {
                    Text(
                        stringResource(
                            R.string.list_chapters_read,
                            chapter.progress,
                            chapter.pages.size,
                            countPagesInMemory
                        ),
                        style = MaterialTheme.typography.body2,
                    )
                }

                AnimatedVisibility(loadingIndicator) {
                    Text(
                        stringResource(R.string.list_chapters_download_progress, downloadPercent),
                        style = MaterialTheme.typography.body2,
                    )
                }

                AnimatedVisibility(queueIndicator) {
                    Text(
                        stringResource(R.string.list_chapters_queue),
                        style = MaterialTheme.typography.body2,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // deleteIndicator
                AnimatedVisibility(deleteIndicator && downloadIndicator.not()) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "indicator for available deleting",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(18.dp),
                    )
                }

                // Date
                Text(
                    chapter.date,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }

        // download button
        AnimatedVisibility(downloadIndicator.not()) {
            IconButton(
                onClick = {
                    com.san.kir.core.download.DownloadService.start(context, chapter)
                },
            ) {
                Icon(Icons.Default.Download, contentDescription = "download button")
            }
        }

        // cancel button
        AnimatedVisibility(downloadIndicator) {
            IconButton(
                onClick = {
                    com.san.kir.core.download.DownloadService.pause(context, chapter)
                },
            ) {
                Icon(Icons.Default.Close, contentDescription = "cancel download button")
            }
        }
    }
}

@Preview
@Composable
fun PreviewChaptersItemContent() {
    ChaptersItemContent(manga = Manga(),
        chapter = Chapter(),
        isSelected = false,
        selectionMode = false,
        onSelectItem = { })
}

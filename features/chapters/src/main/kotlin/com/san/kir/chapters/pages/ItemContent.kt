package com.san.kir.chapters.pages

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.san.kir.chapters.R
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.FullWeightSpacer
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.core.download.DownloadService
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.toast
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.features.viewer.MangaViewer

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyItemScope.ItemContent(
    manga: Manga,
    chapter: Chapter,
    localCountPages: Int,
    isSelected: Boolean,
    selectionMode: Boolean,
    onSelectItem: () -> Unit,
    context: Context = LocalContext.current,
) {
    // Индикатор наличия объектов для удаления
    val deleteIndicator = localCountPages > 0

    // Индикатор ожидания в очереди загрузки
    val queueIndicator = chapter.status == DownloadState.QUEUED
    // Инидикатор загрузки
    val loadingIndicator = chapter.status == DownloadState.LOADING

    val downloadIndicator = queueIndicator || loadingIndicator

    val downloadPercent =
        if (chapter.totalPages == 0) 0
        else chapter.downloadPages * 100 / chapter.totalPages

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .animateItemPlacement()
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
                        // Если режим выделения выключен и происходит загрузка, показать сообщение
                        if (downloadIndicator) {
                            context.toast(R.string.list_chapters_open_is_download)
                        } else {
                            // Иначе проверить, что есть что читать
                            if (chapter.pages.isNullOrEmpty() || chapter.pages.any { it.isBlank() }) {
                                context.longToast(R.string.list_chapters_open_not_exists)
                            } else {
                                MangaViewer.start(context, chapter.id)
                            }
                        }
                        // Выделить элемент
                    } else onSelectItem()
                },
                onLongClick = onSelectItem
            )
            .padding(systemBarsHorizontalPadding())
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Dimensions.small)
        ) {
            // Название
            Text(
                chapter.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.smaller),
            ) {
                // Индикатор загрузки
                AnimatedVisibility(downloadIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(19.dp)
                            .padding(end = Dimensions.smaller),
                        strokeWidth = ProgressIndicatorDefaults.StrokeWidth - 1.5.dp
                    )
                }

                // Прогресс чтения
                AnimatedVisibility(downloadIndicator.not()) {
                    Text(
                        stringResource(
                            R.string.list_chapters_read,
                            chapter.progress,
                            chapter.pages.size,
                            localCountPages
                        ),
                        style = MaterialTheme.typography.body2,
                    )
                }

                // Прогресс загрузки
                AnimatedVisibility(loadingIndicator) {
                    Text(
                        stringResource(R.string.list_chapters_download_progress, downloadPercent),
                        style = MaterialTheme.typography.body2,
                    )
                }

                // Индикатор ожидания
                AnimatedVisibility(queueIndicator) {
                    Text(
                        stringResource(R.string.list_chapters_queue),
                        style = MaterialTheme.typography.body2,
                    )
                }

                // Индикатор наличия страниц в локальной памяти
                AnimatedVisibility(deleteIndicator && downloadIndicator.not()) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "indicator for available deleting",
                        modifier = Modifier
                            .padding(end = Dimensions.smaller)
                            .size(18.dp),
                    )
                }

                FullWeightSpacer()

                // Дата добавления на сайт
                Text(
                    chapter.date,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }

        // Кнопка старта загрузки
        AnimatedVisibility(downloadIndicator.not()) {
            IconButton(
                onClick = {
                    DownloadService.start(context, chapter)
                },
            ) {
                Icon(Icons.Default.Download, contentDescription = "download button")
            }
        }

        // Кнопка отмены загрузки
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

@Preview(showSystemUi = true)
@Composable
internal fun PreviewChaptersItemContent() {

    val manga = Manga(name = "Some Name")
    val chapter =
        Chapter(name = "Some Chapter Name", date = "2000-03-29", progress = 21, totalPages = 32)
    MaterialTheme {
        LazyColumn {
            item {
                ItemContent(
                    manga = manga,
                    chapter = chapter,
                    isSelected = false,
                    selectionMode = false,
                    onSelectItem = { },
                    localCountPages = 28,
                )
            }

            item {
                ItemContent(
                    manga = manga,
                    chapter = chapter,
                    isSelected = true,
                    selectionMode = false,
                    onSelectItem = { },
                    localCountPages = 28,
                )
            }

            item {
                ItemContent(
                    manga = manga,
                    chapter = chapter.copy(status = DownloadState.COMPLETED),
                    isSelected = false,
                    selectionMode = false,
                    onSelectItem = { },
                    localCountPages = 28,
                )
            }

            item {
                ItemContent(
                    manga = manga,
                    chapter = chapter.copy(status = DownloadState.LOADING, isRead = true),
                    isSelected = true,
                    selectionMode = false,
                    onSelectItem = { },
                    localCountPages = 28,
                )
            }

            item {
                ItemContent(
                    manga = manga,
                    chapter = chapter.copy(status = DownloadState.QUEUED, isRead = true),
                    isSelected = false,
                    selectionMode = false,
                    onSelectItem = { },
                    localCountPages = 28,
                )
            }

            item {
                ItemContent(
                    manga = manga,
                    chapter = chapter.copy(status = DownloadState.PAUSED),
                    isSelected = true,
                    selectionMode = false,
                    onSelectItem = { },
                    localCountPages = 28,
                )
            }
        }
    }
}

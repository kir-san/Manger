package com.san.kir.chapters.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.san.kir.chapters.R
import com.san.kir.chapters.ui.chapters.ChaptersEvent
import com.san.kir.chapters.ui.chapters.Filter
import com.san.kir.chapters.ui.chapters.SelectableItem
import com.san.kir.chapters.ui.chapters.Selection
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.FullWeightSpacer
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.extend.countPages
import kotlinx.collections.immutable.ImmutableList

// Страница со списком и инструментами для манипуляции с ним
@Composable
internal fun ListPageContent(
    chapterFilter: ChapterFilter,
    selectionMode: Boolean,
    items: ImmutableList<SelectableItem>,
    navigateToViewer: (Long) -> Unit,
    sendEvent: (ChaptersEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Обертка для корректного отображения элементов если список пустой
        Box(modifier = Modifier.weight(1f)) {

            // Список отображается только если он не пустой
            if (items.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = items,
                        key = { _, ch -> ch.chapter.id },
                    ) { index, chapter ->
                        ItemContent(
                            item = chapter,
                            index = index,
                            selectionMode = selectionMode,
                            navigateToViewer = navigateToViewer,
                            sendEvent = sendEvent
                        )
                    }
                }
            }
        }

        // Нижний бар, скрывается если включен режим выделения
        BottomAnimatedVisibility(selectionMode.not()) {
            BottomOrderBar(chapterFilter) { sendEvent(ChaptersEvent.ChangeFilter(it)) }
        }
    }
}

// Нижний бар управления сортировкой и фильтрацией списка
@Composable
private fun BottomOrderBar(
    currentFilter: ChapterFilter,
    sendEvent: (Filter) -> Unit,
) {
    val allColor = animatedColor(currentFilter.isAll)
    val readColor = animatedColor(currentFilter.isRead)
    val notColor = animatedColor(currentFilter.isNot)
    val reverseRotate by animateFloatAsState(if (currentFilter.isAsc) 0f else 180f)

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .bottomInsetsPadding()
    ) {
        FullWeightSpacer()

        // Смена порядка сортировки
        IconButton(onClick = { sendEvent(Filter.Reverse) }) {
            Icon(
                Icons.Default.Sort,
                contentDescription = "reverse sort",
                modifier = Modifier.rotate(reverseRotate)
            )
        }

        FullWeightSpacer()

        // Кнопка включения отображения всех глав
        IconButton(
            onClick = { sendEvent(Filter.All) },
            modifier = Modifier.padding(horizontal = Dimensions.half)
        ) {
            Icon(
                Icons.Default.SelectAll,
                contentDescription = null,
                tint = allColor
            )
        }

        // Кнопка включения отображения только прочитанных глав
        IconButton(
            onClick = { sendEvent(Filter.Read) },
            modifier = Modifier.padding(horizontal = Dimensions.half)
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = null,
                tint = readColor
            )
        }


        // Кнопка включения отображения только не прочитанных глав
        IconButton(
            onClick = { sendEvent(Filter.NotRead) },
            modifier = Modifier.padding(horizontal = Dimensions.half)
        ) {
            Icon(
                Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = notColor
            )
        }

        FullWeightSpacer()
    }
}

// Анимированая цветовая индикация нажатой кнопки
@Composable
private fun animatedColor(state: Boolean): Color {
    val defaultIconColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val selectedIconColor = Color(0xff36a0da)
    return animateColorAsState(
        targetValue = if (state) selectedIconColor else defaultIconColor,
        label = ""
    ).value
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemContent(
    item: SelectableItem,
    index: Int,
    selectionMode: Boolean,
    navigateToViewer: (Long) -> Unit,
    sendEvent: (ChaptersEvent) -> Unit,
) {
    val countPagesInMemory by produceState(0, item) {
        withIoContext { value = item.chapter.countPages }
    }
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .animateItemPlacement()
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
                    sendEvent = { sendEvent(ChaptersEvent.WithSelected(Selection.Change(index))) }
                ),
                onLongClick = { sendEvent(ChaptersEvent.WithSelected(Selection.Change(index))) }
            )
            .horizontalInsetsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Dimensions.half)
        ) {
            ChapterName(item.chapter.name)


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.quarter),
            ) {

                StatusText(
                    state = item.chapter.status,
                    downloadProgress = item.chapter.downloadProgress,
                    progress = item.chapter.progress,
                    size = item.chapter.pages.size,
                    localCountPages = countPagesInMemory
                )

                FullWeightSpacer()

                // Дата добавления на сайт
                Text(
                    item.chapter.date,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }

        DownloadButton(item.chapter.status) {
            when (it) {
                Download.START -> sendEvent(ChaptersEvent.StartDownload(item.chapter.id))
                Download.STOP -> sendEvent(ChaptersEvent.StopDownload(item.chapter.id))
            }
        }
    }
}

@Composable
private fun StatusText(
    state: DownloadState,
    downloadProgress: Int,
    progress: Int,
    size: Int,
    localCountPages: Int,
) {
    FromBottomToBottomAnimContent(targetState = state) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (it) {
                DownloadState.LOADING -> {
                    LoadingIndicator()
                    LoadingText(downloadProgress)
                }

                DownloadState.QUEUED -> {
                    LoadingIndicator()
                    WaitingText()
                }

                DownloadState.ERROR,
                DownloadState.PAUSED,
                DownloadState.COMPLETED,
                DownloadState.UNKNOWN,
                -> {
                    Text(
                        stringResource(
                            R.string.list_chapters_read,
                            progress,
                            size,
                            localCountPages
                        ),
                        style = MaterialTheme.typography.body2,
                    )

                    BottomAnimatedVisibility(visible = localCountPages > 0) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "indicator for available deleting",
                            modifier = Modifier
                                .padding(end = Dimensions.quarter)
                                .size(Dimensions.default),
                        )
                    }
                }
            }
        }
    }
}

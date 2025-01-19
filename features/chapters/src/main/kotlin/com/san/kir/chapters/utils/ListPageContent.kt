package com.san.kir.chapters.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import com.san.kir.chapters.R
import com.san.kir.chapters.ui.chapters.ChaptersAction
import com.san.kir.chapters.ui.chapters.ChaptersEvent
import com.san.kir.chapters.ui.chapters.Filter
import com.san.kir.chapters.ui.chapters.Items
import com.san.kir.chapters.ui.chapters.SelectableItem
import com.san.kir.chapters.ui.chapters.Selection
import com.san.kir.chapters.ui.chapters.SelectionMode
import com.san.kir.chapters.ui.chapters.convert
import com.san.kir.core.compose.DataIconHelper
import com.san.kir.core.compose.DefaultBottomBar
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.FullWeightSpacer
import com.san.kir.core.compose.HorizontalIconRadioGroup
import com.san.kir.core.compose.IconCounterButton
import com.san.kir.core.compose.IconSize
import com.san.kir.core.compose.RotateToggleButton
import com.san.kir.core.compose.Saver
import com.san.kir.core.compose.ThemedPreview
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.StartAnimatedVisibility
import com.san.kir.core.compose.animation.TopAnimatedVisibility
import com.san.kir.core.compose.animation.rememberFloatAnimatable
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.maxDistanceIn
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ReturnEvents
import com.san.kir.data.models.base.downloadProgress
import com.san.kir.data.models.main.SimplifiedChapter
import com.san.kir.data.models.utils.ChapterFilter
import com.san.kir.data.models.utils.DownloadState

private val SortBarPadding = Dimensions.default
private val SortBarHeightWithPadding = IconSize.height + SortBarPadding * 4


// Страница со списком и инструментами для манипуляции с ним
@Composable
internal fun ListPageContent(
    chapterFilter: ChapterFilter,
    selectionMode: SelectionMode,
    itemsContent: Items,
    sendAction: (Action) -> Unit,
) {
    val itemsCount by remember { derivedStateOf { itemsContent.count } }
    val screenWidth = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenWidth.floatValue = it.boundsInWindow().width }
    ) {
        if (itemsCount > 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = bottomInsetsPadding(
                    bottom = if (selectionMode.enabled) Dimensions.zero else SortBarHeightWithPadding
                )
            ) {
                itemsIndexed(items = itemsContent.items, key = { _, ch -> ch.chapter.id }) { index, chapter ->
                    val memoryPagesCount = itemsContent.memoryPagesCounts[chapter.chapter.id] ?: 0
                    ItemContent(
                        item = chapter,
                        index = index,
                        selectionEnabled = selectionMode.enabled,
                        memoryPagesCount = memoryPagesCount,
                        screenWidth = screenWidth,
                        sendAction = sendAction
                    )
                }
            }
        }

        TopAnimatedVisibility(visible = selectionMode.enabled.not(), modifier = Modifier.align(Alignment.BottomEnd)) {
            BottomOrderBar(chapterFilter) { sendAction(ChaptersAction.ChangeFilter(it)) }
        }

        StartAnimatedVisibility(visible = selectionMode.enabled, modifier = Modifier.align(Alignment.BottomEnd)) {
            SelectionModeBar(selectionMode = selectionMode, sendAction = sendAction)
        }
    }
}

// Нижний бар управления сортировкой и фильтрацией списка
@Composable
private fun BottomOrderBar(
    currentFilter: ChapterFilter,
    sendAction: (Filter) -> Unit,
) {
    val buttons = remember {
        listOf(
            DataIconHelper(Icons.Default.SelectAll, ChapterFilter.ALL_READ_DESC) { it.isAll },
            DataIconHelper(Icons.Default.Visibility, ChapterFilter.IS_READ_DESC) { it.isRead },
            DataIconHelper(Icons.Default.VisibilityOff, ChapterFilter.NOT_READ_DESC) { it.isNot },
        )
    }

    DefaultBottomBar(modifier = Modifier.endInsetsPadding(right = Dimensions.default)) {
        RotateToggleButton(
            icon = Icons.AutoMirrored.Filled.Sort,
            state = currentFilter.isAsc,
            onClick = { sendAction(Filter.Reverse) },
            modifier = Modifier.padding(start = Dimensions.middle),
        )

        HorizontalIconRadioGroup(
            dataHelpers = buttons,
            initialValue = currentFilter,
            onChange = { sendAction(it.convert()) },
            modifier = Modifier.padding(Dimensions.middle)
        )
    }
}

@Composable
internal fun SelectionModeBar(selectionMode: SelectionMode, sendAction: (Action) -> Unit) {
    Row(
        modifier = Modifier
            .bottomInsetsPadding()
            .endInsetsPadding(bottom = Dimensions.default),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier
                .background(SelectedBarColor, DefaultRoundedShape)
                .padding(SelectedBarPadding)
        ) {

            TopAnimatedVisibility(visible = selectionMode.aboveCount > 0) {
                IconCounterButton(
                    icon = Icons.Default.ArrowUpward,
                    contentDescription = "Select Above",
                    counter = "+${selectionMode.aboveCount}",
                    onClick = { sendAction(ChaptersAction.WithSelected(Selection.Above)) },
                )
            }

            BottomAnimatedVisibility(visible = selectionMode.remain > 0) {
                IconCounterButton(
                    icon = Icons.Default.SelectAll,
                    contentDescription = "Select All",
                    counter = "+${selectionMode.remain}",
                    onClick = { sendAction(ChaptersAction.WithSelected(Selection.All)) },
                )
            }

            BottomAnimatedVisibility(visible = selectionMode.belowCount > 0) {
                IconCounterButton(
                    icon = Icons.Default.ArrowDownward,
                    contentDescription = "Select Below",
                    counter = "+${selectionMode.belowCount}",
                    onClick = { sendAction(ChaptersAction.WithSelected(Selection.Below)) },
                )
            }

            IconButton(onClick = { sendAction(ChaptersAction.WithSelected(Selection.Clear)) }) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        }

        Column(
            modifier = Modifier
                .padding(SelectedBarPadding)
                .background(SelectedBarColor, DefaultRoundedShape)
                .padding(SelectedBarPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            IconButton(onClick = { sendAction(ChaptersAction.WithSelected(Selection.Download)) }) {
                Icon(Icons.Default.Download, contentDescription = "Download items")
            }

            BottomAnimatedVisibility(visible = selectionMode.canRemovePages > 0) {
                IconCounterButton(
                    icon = Icons.Default.FolderDelete,
                    contentDescription = "Delete Selection",
                    counter = "${selectionMode.canRemovePages}",
                    onClick = { sendAction(ReturnEvents(ChaptersEvent.ShowDeleteDialog)) },
                )
            }

            BottomAnimatedVisibility(visible = selectionMode.canSetRead > 0) {
                IconCounterButton(
                    icon = Icons.Default.Visibility,
                    contentDescription = null,
                    counter = "${selectionMode.canSetRead}",
                    onClick = { sendAction(ChaptersAction.WithSelected(Selection.SetRead(true))) },
                )
            }

            BottomAnimatedVisibility(visible = selectionMode.canSetUnread > 0) {
                IconCounterButton(
                    icon = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    counter = "${selectionMode.canSetUnread}",
                    onClick = { sendAction(ChaptersAction.WithSelected(Selection.SetRead(false))) },
                )
            }

            BottomAnimatedVisibility(visible = selectionMode.hasReading > 0) {
                IconCounterButton(
                    icon = Icons.Default.LockReset,
                    contentDescription = null,
                    counter = "${selectionMode.hasReading}",
                    onClick = { sendAction(ChaptersAction.WithSelected(Selection.Reset)) },
                )
            }

            IconButton(onClick = { sendAction(ReturnEvents(ChaptersEvent.ShowFullDeleteDialog)) }) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete items from DB")
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemContent(
    item: SelectableItem,
    index: Int,
    selectionEnabled: Boolean,
    memoryPagesCount: Int,
    screenWidth: MutableFloatState,
    sendAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var itemSize by rememberSaveable(stateSaver = Size.Saver, key = "LatestItemSize") { mutableStateOf(Size.Zero) }
    var lastPressPosition by rememberSaveable(stateSaver = Offset.Saver) { mutableStateOf(Offset.Zero) }

    val selectedRadius =
        rememberFloatAnimatable(if (item.selected) lastPressPosition.maxDistanceIn(itemSize) else 0f)

    LaunchedEffect(item.selected, itemSize) {
        selectedRadius.animateTo(if (item.selected) lastPressPosition.maxDistanceIn(itemSize) else 0f)
    }

    LaunchedEffect(Unit) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press && item.selected.not()) {
                lastPressPosition = interaction.pressPosition
            }
        }
    }
    val readingColor = ReadingItemContainerColor
    val selectedColor = SelectedItemContainerColor

    Row(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    clipRect {
                        if (itemSize.isEmpty()) itemSize = size
                        drawRect(
                            color = readingColor,
                            size = size.copy(width = if (item.chapter.isRead) screenWidth.floatValue else 0f)
                        )
                        drawCircle(color = selectedColor, radius = selectedRadius.value, center = lastPressPosition)
                    }
                }
            }
            .fillMaxWidth()
            .horizontalInsetsPadding()
            .animateItem(fadeInSpec = null, fadeOutSpec = null),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val indication = LocalIndication.current
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    interactionSource = interactionSource, indication = indication,
                    onLongClick = { sendAction(ChaptersAction.WithSelected(Selection.Change(index))) },
                    onClick = onClickItem(
                        selectionEnabled,
                        item.chapter,
                        { sendAction(ReturnEvents(ChaptersEvent.ToViewer(it))) },
                        { sendAction(ChaptersAction.WithSelected(Selection.Change(index))) }
                    ),
                )
                .padding(start = Dimensions.default, end = Dimensions.half)
                .padding(vertical = Dimensions.half)
        ) {
            ChapterName(item.chapter.name)

            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.quarter)
                ) {
                    StatusText(
                        item.chapter.status,
                        item.chapter.downloadProgress,
                        item.chapter.progress,
                        item.chapter.pages.size,
                        memoryPagesCount
                    )

                    FullWeightSpacer()

                    Text(
                        text = item.chapter.date,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }

        StartAnimatedVisibility(visible = !selectionEnabled) {
            DownloadButton(
                state = item.chapter.status,
                sendAction = {
                    when (it) {
                        Download.START -> sendAction(ChaptersAction.StartDownload(item.chapter.id))
                        Download.STOP -> sendAction(ChaptersAction.StopDownload(item.chapter.id))
                    }
                },
            )
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
                    Text(stringResource(R.string.reading_progress_format, progress, size, localCountPages))

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

@ThemedPreview
@Composable
private fun PreviewSelectedModeOnListPageContent() {
    MaterialTheme {
        ListPageContent(
            chapterFilter = ChapterFilter.ALL_READ_DESC,
            selectionMode = remember {
                SelectionMode(
                    count = 1,
                    hasReading = 0,
                    canSetRead = 0,
                    canSetUnread = 0,
                    canRemovePages = 0
                )
            },
            itemsContent = remember {
                Items(
                    listOf(
                        createTestItem(1, false),
                        createTestItem(2, false),
                        createTestItem(3, false),
                        createTestItem(4, false),
                        createTestItem(5, false)
                    )
                )
            },
            sendAction = { }
        )
    }
}

@ThemedPreview
@Composable
private fun PreviewSelectedModeOffListPageContent() {
    MaterialTheme {
        ListPageContent(
            chapterFilter = ChapterFilter.ALL_READ_DESC,
            selectionMode = SelectionMode(
                count = 0,
                hasReading = 0,
                canSetRead = 0,
                canSetUnread = 0,
                canRemovePages = 0
            ),
            itemsContent = Items(
                listOf(
                    createTestItem(1, false),
                    createTestItem(2, true),
                    createTestItem(3, true),
                    createTestItem(4, false),
                    createTestItem(5, false)
                )
            ),
            sendAction = { }
        )
    }
}

private fun createTestItem(
    index: Int,
    selected: Boolean = false,
    progress: Int = 0,
    isRead: Boolean = false,
    downloadState: DownloadState = DownloadState.UNKNOWN,
    downloadPages: Int = 0
): SelectableItem {
    return SelectableItem(
        chapter = SimplifiedChapter(
            id = index.toLong(),
            status = downloadState,
            name = "Тестовая глава $index",
            progress = progress,
            isRead = isRead,
            downloadPages = downloadPages,
            pages = emptyList(),
            manga = "Тестовая манга",
            date = "",
            path = "",
            addedTimestamp = 0L
        ),
        selected = selected
    )
}

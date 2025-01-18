package com.san.kir.chapters.ui.latest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.san.kir.chapters.R
import com.san.kir.chapters.utils.ChapterName
import com.san.kir.chapters.utils.DateHeader
import com.san.kir.chapters.utils.DefaultRoundedShape
import com.san.kir.chapters.utils.Download
import com.san.kir.chapters.utils.DownloadButton
import com.san.kir.chapters.utils.LoadingIndicator
import com.san.kir.chapters.utils.LoadingText
import com.san.kir.chapters.utils.MangaType
import com.san.kir.chapters.utils.MenuItem
import com.san.kir.chapters.utils.ReadingItemContainerColor
import com.san.kir.chapters.utils.SelectedBarColor
import com.san.kir.chapters.utils.SelectedBarPadding
import com.san.kir.chapters.utils.SelectedItemContainerColor
import com.san.kir.chapters.utils.WaitingText
import com.san.kir.chapters.utils.date
import com.san.kir.chapters.utils.onClickItem
import com.san.kir.chapters.utils.selectionModeColor
import com.san.kir.core.compose.BottomEndSheets
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.QuarterSpacer
import com.san.kir.core.compose.Saver
import com.san.kir.core.compose.ScreenClear
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.StartAnimatedVisibility
import com.san.kir.core.compose.animation.TopAnimatedVisibility
import com.san.kir.core.compose.animation.rememberFloatAnimatable
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.maxDistanceIn
import com.san.kir.core.compose.startInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.navigation.DialogState
import com.san.kir.core.utils.navigation.EmptyDialogData
import com.san.kir.core.utils.navigation.rememberDialogState
import com.san.kir.core.utils.navigation.show
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.OnEvent
import com.san.kir.core.utils.viewModel.ReturnEvents
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.data.models.base.downloadProgress
import com.san.kir.data.models.main.SimplifiedChapter
import com.san.kir.data.models.utils.DownloadState


private val ItemSize = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LatestScreen(navigateUp: () -> Unit, navigateToViewer: (Long) -> Unit) {
    val holder: LatestStateHolder = stateHolder { LatestViewModel() }
    val state by holder.state.collectAsStateWithLifecycle()
    val selection = holder.selection.collectAsStateWithLifecycle()
    val items = holder.items.collectAsStateWithLifecycle()
    val sendAction = holder.rememberSendAction()
    val lazyListState = rememberLazyListState()
    val dialogState = rememberDialogState<EmptyDialogData>()

    holder.OnEvent { event ->
        when (event) {
            is LatestEvent.ToViewer -> navigateToViewer(event.id)
        }
    }

    ScreenClear(
        topBar = topBar(
            title =
                if (selection.value.enabled) {
                    pluralStringResource(
                        R.plurals.selected_format,
                        selection.value.count,
                        selection.value.count
                    )
                } else {
                    stringResource(R.string.updates_format, state.itemsSize)
                },
            hasAction = state.hasBackgroundWork,
            navigationButton =
                if (selection.value.enabled)
                    NavigationButton.Close { sendAction(LatestAction.UnselectAll) }
                else
                    NavigationButton.Back(navigateUp),
            containerColor = selectionModeColor(selection.value.enabled)
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Content(items, selection, lazyListState, sendAction)

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                DateHeader(
                    lazyListState = lazyListState,
                    itemsState = items,
                    onClick = { if (selection.value.enabled) sendAction(LatestAction.ChangeSelect(it)) },
                    onLongClick = { sendAction(LatestAction.ChangeSelect(it)) }
                )
            }

            TopAnimatedVisibility(
                visible = selection.value.enabled.not() && state.itemsSize > 0,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                DefaultActions(dialogState)
            }

            StartAnimatedVisibility(
                visible = selection.value.enabled,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                SelectionModeBar(selection.value.count, state.itemsSize, sendAction)
            }
        }
    }

    BottomEndSheets(dialogState = dialogState, modifier = Modifier.bottomInsetsPadding()) {
        Column(
            modifier = Modifier
                .endInsetsPadding(horizontal = Dimensions.half)
                .width(IntrinsicSize.Max)
        ) {
            DefaultSpacer()

            BottomAnimatedVisibility(state.hasNewChapters) {
                MenuItem(
                    R.string.download_new_format, state.newChapters.toString(),
                    onClick = {
                        sendAction(LatestAction.DownloadNew)
                        dialogState.dismiss()
                    }
                )
            }

            MenuItem(
                R.string.delete_all, state.itemsSize.toString(),
                onClick = {
                    sendAction(LatestAction.CleanAll)
                    dialogState.dismiss()
                }
            )

            BottomAnimatedVisibility(state.readSize > 0) {
                MenuItem(
                    R.string.delete_reads, state.readSize.toString(),
                    onClick = {
                        sendAction(LatestAction.CleanRead)
                        dialogState.dismiss()
                    }
                )
            }

            BottomAnimatedVisibility(state.downloadedSize > 0) {
                MenuItem(
                    R.string.delete_downloads, state.downloadedSize.toString(),
                    onClick = {
                        sendAction(LatestAction.CleanDownloaded)
                        dialogState.dismiss()
                    }
                )
            }

            DefaultSpacer()
        }
    }
}

@Composable
private fun Content(
    items: State<List<DateContainer>>,
    selection: State<SelectionState>,
    lazyListState: LazyListState,
    sendAction: (Action) -> Unit
) {
    val screenWidth = remember { mutableFloatStateOf(0f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenWidth.floatValue = it.boundsInWindow().width },
        state = lazyListState,
        contentPadding = bottomInsetsPadding(bottom = 72.dp),
    ) {
        items.value.forEachIndexed { index, dateContainer ->
            date(
                date = dateContainer.date,
                onClick = {
                    if (selection.value.enabled) sendAction(LatestAction.ChangeSelect(dateContainer.chaptersIds))
                },
                onLongClick = { sendAction(LatestAction.ChangeSelect(dateContainer.chaptersIds)) },
                modifier = if (index == 0) Modifier else Modifier.padding(top = Dimensions.half)
            )

            dateContainer.mangas.forEach { mangaContainer ->
                manga(
                    name = mangaContainer.manga,
                    date = dateContainer.date,
                    onClick = {
                        if (selection.value.enabled) sendAction(LatestAction.ChangeSelect(mangaContainer.chaptersIds))
                    },
                    onLongClick = { sendAction(LatestAction.ChangeSelect(mangaContainer.chaptersIds)) }
                )

                items(mangaContainer.chapters, key = { "${dateContainer.date}|${it.id}" }) { chapter ->
                    ItemContent(
                        item = chapter,
                        itemSelected = selection.value.hasItem(chapter.id),
                        selectionMode = selection.value.enabled,
                        screenWidth = screenWidth,
                        sendAction = sendAction
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.manga(
    name: String,
    date: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    stickyHeader(key = "$date|$name", contentType = MangaType) {
        Text(
            text = name,
            modifier = Modifier
                .padding(vertical = Dimensions.quarter)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topEndPercent = 30, bottomEndPercent = 30)
                )
                .startInsetsPadding(horizontal = Dimensions.half, vertical = Dimensions.quarter),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun SelectionModeBar(
    selectionCount: Int,
    itemsCount: Int,
    sendAction: (LatestAction) -> Unit
) {
    val allSelected by remember { derivedStateOf { selectionCount == itemsCount } }

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
            BottomAnimatedVisibility(allSelected.not()) {
                IconButton(onClick = { sendAction(LatestAction.SelectAll) }) {
                    Icon(imageVector = Icons.Default.SelectAll, contentDescription = null)
                }
            }

            IconButton(onClick = { sendAction(LatestAction.UnselectAll) }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        }

        Column(
            modifier = Modifier
                .padding(SelectedBarPadding)
                .background(SelectedBarColor, DefaultRoundedShape)
                .padding(SelectedBarPadding)
        ) {
            IconButton(onClick = { sendAction(LatestAction.DownloadSelected) }) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
            }

            IconButton(onClick = { sendAction(LatestAction.RemoveSelected) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}


@Composable
private fun DefaultActions(dialogState: DialogState<EmptyDialogData>) {

    Box(
        modifier = Modifier
            .bottomInsetsPadding()
            .endInsetsPadding(right = Dimensions.default, bottom = Dimensions.default)
            .clip(DefaultRoundedShape)
            .clickable(onClick = dialogState::show)
            .background(color = SelectedBarColor, shape = DefaultRoundedShape)
            .padding(Dimensions.default),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemContent(
    item: SimplifiedChapter,
    itemSelected: Boolean,
    selectionMode: Boolean,
    screenWidth: MutableFloatState,
    sendAction: (Action) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var itemSize by rememberSaveable(stateSaver = Size.Saver, key = "LatestItemSize") { mutableStateOf(Size.Zero) }
    var lastPressPosition by rememberSaveable(stateSaver = Offset.Saver) { mutableStateOf(Offset.Zero) }

    val selectedRadius =
        rememberFloatAnimatable(if (itemSelected) lastPressPosition.maxDistanceIn(itemSize) else 0f)

    LaunchedEffect(itemSelected, itemSize) {
        selectedRadius.animateTo(if (itemSelected) lastPressPosition.maxDistanceIn(itemSize) else 0f)
    }
    LaunchedEffect(Unit) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press && itemSelected.not()) {
                lastPressPosition = interaction.pressPosition
            }
        }
    }


    val readingColor = ReadingItemContainerColor
    val selectedColor = SelectedItemContainerColor
    val indication = LocalIndication.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .drawWithCache {
                onDrawBehind {
                    clipRect {
                        if (itemSize.isEmpty()) itemSize = size
                        drawRect(
                            color = readingColor,
                            size = size.copy(width = if (item.isRead) screenWidth.floatValue else 0f)
                        )
                        drawCircle(color = selectedColor, radius = selectedRadius.value, center = lastPressPosition)
                    }
                }
            }
            .fillMaxWidth()
            .horizontalInsetsPadding()
            .combinedClickable(
                onClick = onClickItem(
                    selectionMode = selectionMode,
                    chapter = item,
                    navigateToViewer = { sendAction(ReturnEvents(LatestEvent.ToViewer(item.id))) },
                    sendAction = { sendAction(LatestAction.changeSelect(item.id)) }
                ),
                onLongClick = { sendAction(LatestAction.changeSelect(item.id)) },
                interactionSource = interactionSource,
                indication = indication,
            )
            .padding(vertical = Dimensions.half)
            .padding(start = Dimensions.default, end = Dimensions.half)
            .animateItem(fadeInSpec = null, fadeOutSpec = null),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .height(ItemSize),
            verticalArrangement = Arrangement.Center,
        ) {
            ChapterName(item.name)
            StatusText(state = item.status, progress = item.downloadProgress)
        }

        StartAnimatedVisibility(selectionMode.not()) {
            DownloadButton(item.status) {
                when (it) {
                    Download.START -> sendAction(LatestAction.StartDownload(item.id))
                    Download.STOP -> sendAction(LatestAction.StopDownload(item.id))
                }
            }
        }
    }
}

@Composable
private fun StatusText(state: DownloadState, progress: Int) {
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
                    QuarterSpacer()
                }

                else -> Unit
            }
        }
    }
}




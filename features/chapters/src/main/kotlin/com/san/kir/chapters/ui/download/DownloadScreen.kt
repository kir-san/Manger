package com.san.kir.chapters.ui.download

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.san.kir.chapters.R
import com.san.kir.chapters.utils.BarPadding
import com.san.kir.chapters.utils.ChapterName
import com.san.kir.chapters.utils.DefaultRoundedShape
import com.san.kir.chapters.utils.Download
import com.san.kir.chapters.utils.DownloadButton
import com.san.kir.chapters.utils.MenuItem
import com.san.kir.chapters.utils.SelectedBarColor
import com.san.kir.chapters.utils.SelectedBarPadding
import com.san.kir.core.compose.BottomEndSheets
import com.san.kir.core.compose.CircleLogo
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.IconSize
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.EndAnimatedVisibility
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.FromTopToTopAnimContent
import com.san.kir.core.compose.animation.StartAnimatedVisibility
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.startInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.internet.NetworkState
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.navigation.EmptyDialogData
import com.san.kir.core.utils.navigation.rememberDialogState
import com.san.kir.core.utils.navigation.show
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.OnEvent
import com.san.kir.core.utils.viewModel.ReturnEvents
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.data.models.main.DownloadItem
import com.san.kir.data.models.utils.DownloadState


private val InnerBarPadding = Dimensions.middle
private val BarHeightWithPadding = IconSize.height + BarPadding + InnerBarPadding * 2

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun DownloadsScreen(navigateUp: () -> Unit) {
    val holder: DownloadsStateHolder = stateHolder { DownloadsViewModel() }
    val state by holder.state.collectAsStateWithLifecycle()
    val sendAction = holder.rememberSendAction()
    val dialogState = rememberDialogState<EmptyDialogData>()

    holder.OnEvent { event ->
        when (event) {
            DownloadsEvent.ShowDeleteMenu -> dialogState.show()
            DownloadsEvent.HideDeleteMenu -> dialogState.dismiss()
        }
    }

    ScreenList(
        additionalPadding = Dimensions.zero,
        contentPadding = bottomInsetsPadding(bottom = BarHeightWithPadding),
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.downloader_format, state.loadingCount),
            subtitle = stringResource(
                R.string.subtitle_format, state.stoppedCount, state.completedCount
            ),
        ),
        bottomContent = { BottomManagerBar(state, sendAction) }
    ) {
        state.items.forEach { group ->
            stickyHeader(key = group.groupName) {
                Text(
                    text = stringResource(group.groupName),
                    modifier = Modifier
                        .padding(top = Dimensions.default, bottom = Dimensions.quarter)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(0, 30, 30, 0))
                        .startInsetsPadding(horizontal = Dimensions.half, vertical = Dimensions.quarter),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            items(items = group.items, key = { it.id }) { item ->
                ItemView(item, sendAction)
            }
        }
    }

    BottomEndSheets(dialogState = dialogState, modifier = Modifier.bottomInsetsPadding()) {
        ClearDownloadsMenu(state, sendAction)
    }
}

@Composable
private fun BottomManagerBar(state: DownloadsState, sendAction: (Action) -> Unit) {
    FromBottomToBottomAnimContent(targetState = state.network) {
        when (it) {
            NetworkState.NOT_WIFI -> {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomInsetsPadding()
                ) {
                    Text(
                        stringResource(com.san.kir.background.R.string.wifi_off),
                        modifier = Modifier.horizontalInsetsPadding()
                    )
                }
            }

            NetworkState.NOT_CELLULAR -> {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomInsetsPadding()
                ) {
                    Text(
                        stringResource(com.san.kir.background.R.string.internet_off),
                        modifier = Modifier.horizontalInsetsPadding()
                    )
                }
            }

            NetworkState.OK -> {
                BottomAnimatedVisibility(state.allCount > 0) {
                    Row(
                        modifier = Modifier
                            .bottomInsetsPadding(bottom = Dimensions.default)
                            .endInsetsPadding(right = Dimensions.default)
                            .background(SelectedBarColor, DefaultRoundedShape)
                            .padding(SelectedBarPadding)
                    ) {

                        StartAnimatedVisibility(state.stoppedCount > 0) {
                            Row {
                                DefaultSpacer()
                                IconButton(onClick = { sendAction(DownloadsAction.StartAll) }) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                }
                            }
                        }

                        StartAnimatedVisibility(state.loadingCount > 0) {
                            Row {
                                DefaultSpacer()
                                IconButton(onClick = { sendAction(DownloadsAction.StopAll) }) {
                                    Icon(Icons.Filled.Stop, contentDescription = null)
                                }
                            }
                        }

                        EndAnimatedVisibility(state.canClearedCount > 0) {
                            Row {
                                DefaultSpacer()
                                IconButton(onClick = { sendAction(ReturnEvents(DownloadsEvent.ShowDeleteMenu)) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            }
                        }

                        DefaultSpacer()
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearDownloadsMenu(state: DownloadsState, sendAction: (Action) -> Unit) {
    Column(
        modifier = Modifier
            .endInsetsPadding(horizontal = Dimensions.half)
            .width(IntrinsicSize.Max)
    ) {
        DefaultSpacer()
        BottomAnimatedVisibility(state.completedCount > 0) {
            MenuItem(R.string.clean_completed, state.completedCount.toString()) {
                sendAction(DownloadsAction.CompletedClear)
                sendAction(ReturnEvents(DownloadsEvent.HideDeleteMenu))
            }
        }
        BottomAnimatedVisibility(state.stoppedCount > 0) {
            MenuItem(R.string.clean_paused, state.stoppedCount.toString()) {
                sendAction(DownloadsAction.PausedClear)
                sendAction(ReturnEvents(DownloadsEvent.HideDeleteMenu))
            }
        }
        BottomAnimatedVisibility(state.errorCount > 0) {
            MenuItem(R.string.clean_with_error, state.errorCount.toString()) {
                sendAction(DownloadsAction.ErrorClear)
                sendAction(ReturnEvents(DownloadsEvent.HideDeleteMenu))
            }
        }
        BottomAnimatedVisibility(state.allCount > 1) {
            MenuItem(R.string.clean_all, state.canClearedCount.toString()) {
                sendAction(DownloadsAction.ClearAll)
                sendAction(ReturnEvents(DownloadsEvent.HideDeleteMenu))
            }
        }
        DefaultSpacer()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemView(item: DownloadItem, sendAction: (DownloadsAction) -> Unit) {
    val totalPages by remember { derivedStateOf { item.pages.size } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .horizontalInsetsPadding(horizontal = Dimensions.default)
            .padding(top = Dimensions.half),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            CircleLogo(logoUrl = item.logo)

            if (item.status == DownloadState.ERROR)
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.size(Dimensions.Image.small)
                ) {
                    Icon(
                        painterResource(R.drawable.unknown),
                        contentDescription = "unknown",
                        modifier = Modifier.size(Dimensions.default)
                    )
                }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = Dimensions.half),
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                if (item.needShowMangaName) MangaName(item.manga)

                ChapterName(item.name)

                StatusText(
                    state = item.status,
                    size = item.size,
                    time = item.time(),
                    downloadPages = item.downloadPages,
                    totalPages = totalPages
                )
            }

            ProgressIndicator(state = item.status, progress = item.progress)
        }

        DownloadButton(state = item.status) {
            when (it) {
                Download.START -> sendAction(DownloadsAction.StartDownload(item.id))
                Download.STOP -> sendAction(DownloadsAction.StopDownload(item.id))
            }
        }
    }
}

@Composable
private fun ProgressIndicator(state: DownloadState, progress: Float) {
    FromTopToTopAnimContent(targetState = state) {
        when (it) {
            DownloadState.QUEUED -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = Dimensions.quarter)
                        .height(Dimensions.quarter)
                        .fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                )
            }

            DownloadState.LOADING -> {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .padding(top = Dimensions.quarter)
                        .height(Dimensions.quarter)
                        .fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {},
                )
            }

            else -> {
            }
        }
    }
}

@Composable
private fun StatusText(state: DownloadState, size: String, time: String, downloadPages: Int, totalPages: Int) {
    FromTopToTopAnimContent(targetState = state) {
        when (it) {
            DownloadState.COMPLETED ->
                Text(stringResource(R.string.download_final_format, size, time))

            else ->
                Text(
                    stringResource(
                        R.string.concat_format,
                        stringResource(R.string.download_progress_format, downloadPages, totalPages),
                        stringResource(R.string.size_format, size)
                    )
                )
        }
    }
}

@Composable
private fun BottomInfoBar(@StringRes textId: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(
                    topStart = Dimensions.default,
                    topEnd = Dimensions.default,
                )
            ),
    ) {
        Text(
            text = stringResource(textId),
            modifier = Modifier
                .bottomInsetsPadding()
                .padding(Dimensions.default),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
private fun MangaName(manga: String) {
    Text(manga, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

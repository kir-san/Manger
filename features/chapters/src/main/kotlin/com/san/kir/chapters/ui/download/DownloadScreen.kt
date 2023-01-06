package com.san.kir.chapters.ui.download

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.chapters.R
import com.san.kir.chapters.utils.ChapterName
import com.san.kir.chapters.utils.Download
import com.san.kir.chapters.utils.DownloadButton
import com.san.kir.core.compose.CircleLogo
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.ExpandedMenu
import com.san.kir.core.compose.FullWeightSpacer
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.FromTopToTopAnimContent
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.internet.NetworkState
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.extend.DownloadChapter

@Composable
fun DownloadsScreen(navigateUp: () -> Boolean) {
    val viewModel: DownloadsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenContent(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.main_menu_downloader_count, state.loadingCount),
            subtitle = stringResource(
                R.string.download_activity_subtitle, state.stoppedCount, state.completedCount
            ),
        ), additionalPadding = Dimensions.zero
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(items = state.items, key = { it.id }) { item ->
                ItemView(item, viewModel::sendEvent)
            }
        }

        BottomScreenPart(state = state.network, sendEvent = viewModel::sendEvent)
    }
}

@Composable
private fun BottomScreenPart(state: NetworkState, sendEvent: (DownloadsEvent) -> Unit) {
    FromBottomToBottomAnimContent(targetState = state) {
        when (it) {
            NetworkState.NOT_WIFI     -> {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomInsetsPadding()
                ) {
                    Text(stringResource(com.san.kir.background.R.string.wifi_off))
                }
            }

            NetworkState.NOT_CELLURAR -> {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomInsetsPadding()
                ) {
                    Text(stringResource(com.san.kir.background.R.string.internet_off))
                }
            }

            NetworkState.OK           -> {
                var showMenu by remember { mutableStateOf(false) }

                // Массовое управление загрузками
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomInsetsPadding()
                ) {
                    FullWeightSpacer()

                    // Кнопка включения отображения всех глав
                    IconButton(onClick = { sendEvent(DownloadsEvent.StartAll) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    }

                    // Кнопка паузы
                    IconButton(
                        onClick = { sendEvent(DownloadsEvent.StopAll) },
                        modifier = Modifier.padding(start = Dimensions.default)
                    ) { Icon(Icons.Filled.Stop, contentDescription = null) }

                    FullWeightSpacer()

                    // Кнопка очистки списка загрузок
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        ClearDownloadsMenu(showMenu, { showMenu = false }, sendEvent)
                    }

                    FullWeightSpacer()
                }
            }
        }
    }
}

@Composable
private fun ClearDownloadsMenu(
    expanded: Boolean,
    onClose: () -> Unit,
    sendEvent: (DownloadsEvent) -> Unit,
) {
    ExpandedMenu(expanded = expanded, onCloseMenu = onClose) {
        MenuText(R.string.download_activity_option_submenu_clean_completed) {
            sendEvent(DownloadsEvent.CompletedClear)
        }
        MenuText(R.string.download_activity_option_submenu_clean_paused) { sendEvent(DownloadsEvent.PausedClear) }
        MenuText(R.string.download_activity_option_submenu_clean_error) { sendEvent(DownloadsEvent.ErrorClear) }
        MenuText(R.string.download_activity_option_submenu_clean_all) { sendEvent(DownloadsEvent.ClearAll) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemView(
    item: DownloadChapter,
    sendEvent: (DownloadsEvent) -> Unit,
) {
    val ctx: Context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement()
            .horizontalInsetsPadding()
            .padding(top = Dimensions.half)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            CircleLogo(logoUrl = item.logo)

            if (item.isError)
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
            verticalArrangement = Arrangement.Center
        ) {
            ProvideTextStyle(MaterialTheme.typography.subtitle2) {
                MangaName(item.manga)
                ChapterName(item.name)

                StatusText(
                    state = item.status,
                    size = item.size,
                    time = item.time(ctx),
                    downloadPages = item.downloadPages,
                    totalPages = item.totalPages
                )
            }


            ProgressIndicator(state = item.status, progress = item.progress)
        }

        DownloadButton(state = item.status) {
            when (it) {
                Download.START -> sendEvent(DownloadsEvent.StartDownload(item.id))
                Download.STOP  -> sendEvent(DownloadsEvent.StopDownload(item.id))
            }
        }
    }
}

@Composable
private fun ProgressIndicator(state: DownloadState, progress: Float) {
    FromTopToTopAnimContent(targetState = state) {
        when (it) {
            DownloadState.QUEUED  -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = Dimensions.quarter)
                        .height(Dimensions.half)
                        .fillMaxWidth(),
                )
            }

            DownloadState.LOADING -> {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .padding(top = Dimensions.quarter)
                        .height(Dimensions.half)
                        .fillMaxWidth(),
                )
            }

            else                  -> {
            }
        }
    }
}

@Composable
private fun StatusText(
    state: DownloadState,
    size: String,
    time: String,
    downloadPages: Int,
    totalPages: Int,
) {
    FromTopToTopAnimContent(targetState = state) {
        when (it) {
            DownloadState.COMPLETED -> {
                Text(stringResource(R.string.download_item_final_size_with_time, size, time))
            }

            else                    -> {
                val pages = stringResource(
                    R.string.download_item_progress_text, downloadPages, totalPages
                )
                val downloadSize = stringResource(R.string.download_item_progress_size, size)
                Text(stringResource(R.string.download_item_progress, pages, downloadSize))
            }
        }
    }
}

@Composable
private fun MangaName(manga: String) {
    Text(
        manga,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

package com.san.kir.manger.ui.application_navigation.download

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.ExpandedMenu
import com.san.kir.core.compose_utils.FullWeightSpacer
import com.san.kir.core.compose_utils.ScreenPadding
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.download.DownloadService
import com.san.kir.core.internet.NetworkState
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.bytesToMb
import com.san.kir.core.utils.formatDouble
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.manger.R
import com.san.kir.core.utils.TimeFormat

@Composable
fun DownloadScreen(
    navigateUp: () -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    ScreenPadding(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.main_menu_downloader_count, viewModel.loadingCount),
            subtitle = stringResource(
                R.string.download_activity_subtitle,
                viewModel.stoppedCount,
                viewModel.completedCount
            ),
        ), additionalPadding = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 10.dp, start = 10.dp, end = 10.dp)
            ) {
                items(count = viewModel.items.size, key = { i -> viewModel.items[i].id }) { index ->
                    ItemView(viewModel.items[index], viewModel)
                }
            }

            when (viewModel.network) {
                NetworkState.NOT_WIFI -> {
                    Snackbar(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.download_view_wifi_off))
                    }
                }
                NetworkState.NOT_CELLURAR -> {
                    Snackbar(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.download_view_internet_off))
                    }
                }
                NetworkState.OK -> {
                    val (expandValue, expandSetter) = remember { mutableStateOf(false) }

                    // Массовое управление загрузками
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Vertical))
                    ) {
                        FullWeightSpacer()

                        // Кнопка включения отображения всех глав
                        IconButton(onClick = { DownloadService.startAll(context) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        }

                        // Кнопка паузы
                        IconButton(
                            onClick = { DownloadService.pauseAll(context) },
                            modifier = Modifier.padding(start = 16.dp)
                        ) { Icon(Icons.Filled.Stop, contentDescription = null) }

                        FullWeightSpacer()

                        // Кнопка очистки списка загрузок
                        IconButton(onClick = { expandSetter(true) }) {
                            ClearDownloadsMenu(expandValue, expandSetter)
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }

                        FullWeightSpacer()
                    }
                }
            }
        }

    }
}

@Composable
private fun ItemView(
    item: Chapter,
    viewModel: DownloadViewModel,
    ctx: Context = LocalContext.current,
) {
    val heightSize = 40.dp
    val errorSize = 15.dp

    val manga by viewModel.manga(item).collectAsState(Manga())

//    val offsetX = remember { Animatable(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(systemBarsHorizontalPadding())
        /* .swipeToDelete(offsetX, 100f) {
             viewModel.remove(item)
         }*/
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
            Image(
                rememberImage(manga.logo),
                modifier = Modifier
                    .padding(end = 5.dp)
                    .clip(CircleShape)
                    .size(heightSize),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            if (item.isError) Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.size(heightSize)
            ) {
                Icon(
                    painterResource(R.drawable.unknown),
                    contentDescription = "unknown",
                    modifier = Modifier.size(errorSize)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.download_item_name, item.manga, item.name), maxLines = 1)
            Text(
                when (item.status) {
                    DownloadState.COMPLETED -> {
                        val time = TimeFormat(item.totalTime / 1000)
                        stringResource(
                            R.string.download_item_final_size_with_time,
                            formatDouble(bytesToMb(item.downloadSize)),
                            time.toString(ctx)
                        )
                    }
                    else -> {
                        val pages = stringResource(
                            R.string.download_item_progress_text,
                            item.downloadPages,
                            item.totalPages
                        )
                        val size = stringResource(
                            R.string.download_item_progress_size,
                            formatDouble(bytesToMb(item.downloadSize))
                        )
                        stringResource(R.string.download_item_progress, pages, size)
                    }
                }
            )

            when (item.status) {
                DownloadState.QUEUED -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(10.dp)
                            .fillMaxWidth(),
                    )
                }
                DownloadState.LOADING -> {
                    LinearProgressIndicator(
                        progress = if (item.totalPages != 0) item.downloadPages.toFloat() / item.totalPages.toFloat()
                        else 0F,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(10.dp)
                            .fillMaxWidth(),
                    )
                }
                else -> {
                }
            }

        }

        when (item.status) {
            DownloadState.PAUSED -> {
                IconButton(onClick = {
                    DownloadService.start(ctx, item)
                }) {
                    Icon(Icons.Default.Download, contentDescription = "download button")
                }
            }
            DownloadState.QUEUED, DownloadState.LOADING -> {
                IconButton(onClick = {
                    DownloadService.pause(ctx, item)
                }) {
                    Icon(Icons.Default.Close, contentDescription = "cancel download button")
                }
            }
            else -> {
            }
        }
    }
}

@Composable
fun ClearDownloadsMenu(
    expanded: Boolean,
    changeExpand: (Boolean) -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
) {
    ExpandedMenu(expanded = expanded, onCloseMenu = { changeExpand(false) }) {
        MenuText(
            id = R.string.download_activity_option_submenu_clean_completed,
            onClick = viewModel::clearCompletedDownloads,
        )
        MenuText(
            id = R.string.download_activity_option_submenu_clean_paused,
            onClick = viewModel::clearPausedDownloads,
        )
        MenuText(
            id = R.string.download_activity_option_submenu_clean_error,
            onClick = viewModel::clearErrorDownloads,
        )
        MenuText(
            id = R.string.download_activity_option_submenu_clean_all,
            onClick = viewModel::clearAllDownloads,
        )
    }
}


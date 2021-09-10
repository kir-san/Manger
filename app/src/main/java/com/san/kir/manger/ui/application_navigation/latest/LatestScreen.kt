package com.san.kir.manger.ui.application_navigation.latest

import android.app.Application
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.work.WorkManager
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.manger.R
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.room.entities.countPages
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.MenuText
import com.san.kir.manger.ui.utils.TopBarScreenList
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.utils.extensions.delChapters
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.quantitySimple
import com.san.kir.manger.workmanager.AllLatestClearWorker
import com.san.kir.manger.workmanager.DownloadedLatestClearWorker
import com.san.kir.manger.workmanager.LatestClearWorker
import com.san.kir.manger.workmanager.ReadLatestClearWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Composable
fun LatestScreen(
    nav: NavHostController,
    viewModel: LatestViewModel = hiltViewModel()
) {
    val allItems by viewModel.allItems.collectAsState(emptyList())
    val selectedItems by viewModel.selectedItems.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()

    var isAction by remember { mutableStateOf(false) }

    TopBarScreenList(
        additionalPadding = 0.dp,
        navHostController = nav,
        title = if (selectionMode) {
            LocalContext.current.quantitySimple(
                R.plurals.list_chapters_action_selected, selectedItems.count { it }
            )
        } else {
            stringResource(R.string.main_menu_latest_count, allItems.size)
        },
        actions = { LatestActions(viewModel) }
    ) {
        item {
            if (isAction) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        itemsIndexed(allItems) { index, chapter ->
            LatestItemContent(
                chapter = chapter,
                isSelected = selectedItems[index],
                index = index,
                viewModel = viewModel
            )
        }
    }

    val context = LocalContext.current

    LaunchedEffect("collect") {
        WorkManager
            .getInstance(context)
            .getWorkInfosByTagLiveData(LatestClearWorker.tag)
            .asFlow()
            .filter { works -> works.isNotEmpty() }
            .map { works -> !works.all { it.state.isFinished } }
            .collect { isAction = it }
    }
}

@Composable
private fun LatestActions(
    viewModel: LatestViewModel,
    context: Context = LocalContext.current
) {
    val selectionMode by viewModel.selectionMode.collectAsState()
    val hasNewChapters by viewModel.hasNewChapters.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    if (selectionMode) {
        MenuIcon(icon = Icons.Default.Delete) {
            viewModel.deleteSelectedItems()
        }
    } else {
        MenuIcon(icon = Icons.Default.MoreVert) {
            expanded = true
        }
    }


    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

        if (hasNewChapters) {
            MenuText(R.string.latest_chapter_download_new) {
                expanded = false
                viewModel.downloadNewChapters()
            }
        }

        MenuText(R.string.latest_chapter_clean) {
            expanded = false
            LatestClearWorker.addTask<AllLatestClearWorker>(context)
        }

        MenuText(R.string.latest_chapter_clean_read) {
            expanded = false
            LatestClearWorker.addTask<ReadLatestClearWorker>(context)
        }

        MenuText(R.string.latest_chapter_clean_download) {
            expanded = false
            LatestClearWorker.addTask<DownloadedLatestClearWorker>(context)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun LatestItemContent(
    chapter: Chapter,
    isSelected: Boolean,
    index: Int,
    viewModel: LatestViewModel,
    context: Context = LocalContext.current,
) {
    val selectionMode by viewModel.selectionMode.collectAsState()
    val downloadItem by viewModel.getDownloadItem(chapter).collectAsState(DownloadItem())

    val downloadIndicator by remember(downloadItem) {
        mutableStateOf(
            downloadItem.status == DownloadStatus.queued
                    || downloadItem.status == DownloadStatus.loading
        )
    }
    val queueIndicator by remember(downloadItem) {
        mutableStateOf(downloadItem.status == DownloadStatus.queued)
    }
    val loadingIndicator by remember(downloadItem) {
        mutableStateOf(downloadItem.status == DownloadStatus.loading)
    }
    val downloadPercent by remember(downloadItem) {
        mutableStateOf(
            if (downloadItem.totalPages == 0) 0
            else downloadItem.downloadPages * 100 / downloadItem.totalPages
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
                    if (selectionMode.not()) {
                        if (downloadIndicator) {
                            context.toast(R.string.list_chapters_open_is_download)
                        } else {
                            if (chapter.pages.isNullOrEmpty() || chapter.pages.any { it.isBlank() }) {
                                context.longToast(R.string.list_chapters_open_not_exists)
                            } else {
                                /*context.startActivity<ViewerActivity>(
                                    "chapter" to chapter,
                                    "is" to manga.isAlternativeSort
                                )*/
                            }
                        }
                    } else viewModel.onSelectItem(index)
                },
                onLongClick = {
                    log = "longClick"
                    viewModel.onSelectItem(index)
                }
            ),
    )
    {
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
                Text(
                    chapter.manga,
                    style = MaterialTheme.typography.body2,
                )


                Spacer(modifier = Modifier.weight(1f))

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
                    DownloadService.addOrStart(context, chapter.toDownloadItem())
                },
            ) {
                Icon(Icons.Default.Download, contentDescription = "download button")
            }
        }

        // cancel button
        AnimatedVisibility(downloadIndicator) {
            IconButton(
                onClick = {
                    DownloadService.pause(context, chapter.toDownloadItem())
                },
            ) {
                Icon(Icons.Default.Close, contentDescription = "cancel download button")
            }
        }
    }
}


@HiltViewModel
class LatestViewModel @Inject constructor(
    private val context: Application,
    private val chapterDao: ChapterDao,
    private val downloadDao: DownloadDao,
) : ViewModel() {
    private val _allITems = MutableStateFlow(listOf<Chapter>())
    val allItems = _allITems.asStateFlow()

    private val _newChapters = MutableStateFlow(listOf<Chapter>())
    private val _hasNewChapters = MutableStateFlow(false)
    val hasNewChapters = _hasNewChapters.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            chapterDao.loadAllItems()
                .onEach { list -> _allITems.update { list } }
                .map { list ->
                    list.filter { it.isInUpdate }
                        .filter { !it.isRead }
                        .filter { it.action == ChapterStatus.DOWNLOADABLE }
                }
                .collect { list ->
                    _newChapters.update { list }
                    _hasNewChapters.update { list.isNotEmpty() }
                }
        }

        // обновление размера списка выделеных элементов
        viewModelScope.launch(Dispatchers.Default) {
            allItems.map { it.count() }.collect { count ->
                _selectedItems.update { old ->
                    if (old.count() != count) {
                        List(count) { false }
                    } else {
                        old
                    }
                }
            }
        }

        // активация и дезактивация режима выделения
        viewModelScope.launch(Dispatchers.Default) {
            combine(
                _selectedItems.map { array -> array.count { it } },
                _selectionMode
            ) { selectedCount, mode ->
                if (selectedCount > 0 && mode.not()) {
                    _selectionMode.update { true }
                } else if (selectedCount <= 0 && mode) {
                    _selectionMode.update { false }
                }
            }.collect()
        }
    }

    fun downloadNewChapters() = viewModelScope.launch(Dispatchers.Default) {
        _newChapters.value.onEach { chapter ->
            DownloadService.addOrStart(context, chapter.toDownloadItem())
        }
    }

    fun getDownloadItem(item: Chapter) = downloadDao.loadItem(item.site).filterNotNull()

    // for selection mode
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()
    private val _selectedItems = MutableStateFlow(listOf<Boolean>())
    val selectedItems = _selectedItems.asStateFlow()
    fun onSelectItem(index: Int) = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.update { old ->
            old.toMutableList().apply { set(index, get(index).not()) }
        }
    }

    fun deleteSelectedItems() = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.value.zip(allItems.value).forEachIndexed { i, (b, chapter) ->
            if (b) {
                chapter.isInUpdate = false
                chapterDao.update(chapter)
            }
        }
    }
}

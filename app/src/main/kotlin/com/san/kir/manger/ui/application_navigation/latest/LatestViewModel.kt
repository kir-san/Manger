package com.san.kir.manger.ui.application_navigation.latest

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.action
import com.san.kir.manger.foreground_work.services.DownloadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LatestViewModel @Inject constructor(
    private val context: Application,
    private val chapterDao: ChapterDao,
) : ViewModel() {

    var allItems by mutableStateOf(listOf<Chapter>())
        private set

    private var newChapters by mutableStateOf(listOf<Chapter>())

    var hasNewChapters by mutableStateOf(false)
        private set

    var selectionMode by mutableStateOf(false)
        private set

    var selectedItems by mutableStateOf(listOf<Boolean>())
        private set


    init {
        viewModelScope.defaultLaunch {
            chapterDao.loadAllItems()
                .onEach { list ->
                    withMainContext { allItems = list }
                    // обновление размера списка выделеных элементов
                    if (list.count() != selectedItems.count())
                        selectedItems = List(list.count()) { false }
                }
                .map { list ->
                    list.filter { it.isInUpdate }
                        .filter { !it.isRead }
                        .filter { it.action == ChapterStatus.DOWNLOADABLE }
                }
                .collect { list ->
                    withMainContext {
                        newChapters = list
                        hasNewChapters = list.isNotEmpty()
                    }
                }
        }

        // активация и дезактивация режима выделения
        snapshotFlow { selectionMode to selectedItems }
            .map { (mode, list) -> mode to list.count { it } }
            .onEach { (mode, count) ->
                if (count > 0 && mode.not()) {
                    withMainContext {
                        selectionMode = true
                    }
                } else if (count <= 0 && mode) {
                    withMainContext {
                        selectionMode = false
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun downloadNewChapters() = viewModelScope.defaultLaunch {
        newChapters.onEach { chapter ->
            DownloadService.start(context, chapter)
        }
    }

    fun onSelectItem(index: Int) = viewModelScope.defaultLaunch {
        selectedItems = selectedItems.toMutableList().apply { set(index, get(index).not()) }
    }

    fun deleteSelectedItems() = viewModelScope.defaultLaunch {
        selectedItems.zip(allItems).forEachIndexed { _, (b, chapter) ->
            if (b) {
                chapter.isInUpdate = false
                chapterDao.update(chapter)
            }
        }
    }
}

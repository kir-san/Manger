package com.san.kir.manger.ui.application_navigation.storage.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.StorageDao
import com.san.kir.manger.room.dao.searchNewItems
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.utils.extensions.getFullPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val context: Application,
    private val storageDao: StorageDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) : ViewModel() {
    private var mangaList = listOf<Manga>()

    private val _state = MutableStateFlow(StorageViewState())
    val state: StateFlow<StorageViewState>
        get() = _state

    val allStorage = Pager(
        config = PagingConfig(
            pageSize = 30,
            enablePlaceholders = true,
            maxSize = 100,
        )
    ) {
        storageDao.allItemsBySizeFull()
    }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            mangaList = mangaDao.getItems()
            storageDao.flowItems()
                .catch { t -> throw t }
                .collect { items ->
                    val sizes = items.sumOf { it.sizeFull }
                    val count = items.count()

                    _state.value = StorageViewState(
                        storageSize = sizes,
                        storageCounts = count,
                    )
                }
        }

        viewModelScope.launch(Dispatchers.Default) {
            storageDao.searchNewItems(mangaDao, chapterDao)
        }
    }

    fun mangaFromPath(path: String): Manga? {
        return mangaList.firstOrNull { getFullPath(it.path) == getFullPath(path) }
    }

    fun delete(item: Storage) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                storageDao.delete(item)
                getFullPath(item.path).deleteRecursively()
            }.onFailure {
                context.longToast(it.toString())
            }
        }
    }

}

data class StorageViewState(
    val storageSize: Double = 0.0,
    val storageCounts: Int = 0,
)

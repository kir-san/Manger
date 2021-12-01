package com.san.kir.manger.ui.application_navigation.storage.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.data.room.dao.ChapterDao
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.dao.StorageDao
import com.san.kir.manger.data.room.dao.searchNewItems
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.manger.data.room.entities.Storage
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.utils.extensions.getFullPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
        defaultLaunchInVM {
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

        defaultLaunchInVM {
            storageDao.searchNewItems(mangaDao, chapterDao)
        }
    }

    fun mangaFromPath(path: String): Flow<Manga?> {
        return mangaDao.loadItems()
            .distinctUntilChanged()
            .map { list -> list.firstOrNull { getFullPath(it.path) == getFullPath(path) } }
    }

    fun delete(item: Storage) {
        defaultLaunchInVM {
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

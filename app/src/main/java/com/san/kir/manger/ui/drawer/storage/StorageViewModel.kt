package com.san.kir.manger.ui.drawer.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.longToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StorageViewModel(app: Application) : AndroidViewModel(app) {
    private val storageDao = getDatabase(app).storageDao
    private var mangaList = listOf<Manga>()

    private val _state = MutableStateFlow(StorageViewState())
    val state: StateFlow<StorageViewState>
        get() = _state

    init {
        viewModelScope.launch(Dispatchers.Default) {
            mangaList = getDatabase(app).mangaDao.getItems()
            storageDao.flowItems()
                .catch { t -> throw t }
                .collect { items ->
                    val sizes = items.sumOf { it.sizeFull }
                    val count = items.count()

                    _state.value = StorageViewState(
                        storageSize = sizes,
                        storageCounts = count,
                        items = items,
                    )
                }
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
                getApplication<Application>().longToast(it.toString())
            }
        }
    }

}

data class StorageViewState(
    val storageSize: Double = 0.0,
    val storageCounts: Int = 0,
    val items: List<Storage> = emptyList(),
)

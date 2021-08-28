package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.StorageDao
import com.san.kir.manger.room.dao.searchNewItems
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.room.entities.getSizeAndIsNew
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.shortPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaStorageViewModel @Inject constructor(
    private val storageDao: StorageDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.Default) {
            storageDao.searchNewItems(mangaDao, chapterDao)
        }
    }

    fun generalSize(): Flow<Double> =
        storageDao
            .flowItems()
            .map { list -> list.sumOf { item -> item.sizeFull } }

    fun storageWhere(shortPath: String): Flow<Storage> {
        return storageDao.flowItem(getFullPath(shortPath).shortPath)
            .mapNotNull { it }
            .onEach { storage ->
                storage.let { s ->
                    viewModelScope.launch(Dispatchers.Default) {
                        val updatedS = s.getSizeAndIsNew(mangaDao, chapterDao)

                        if (s.sizeFull != updatedS.sizeFull || s.sizeRead != updatedS.sizeRead) {
                            storageDao.update(updatedS)
                        }
                    }
                }
            }
    }
}


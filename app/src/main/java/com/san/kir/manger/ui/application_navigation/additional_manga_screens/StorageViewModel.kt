package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.lifecycle.ViewModel
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.StorageDao
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb
import com.san.kir.manger.utils.extensions.shortPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val storageDao: StorageDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) : ViewModel() {

    fun generalSize(): Flow<Double> = storageDao.flowItems()
        .map { list -> list.sumOf { item -> item.sizeFull } }

    fun storageWhere(shortPath: String): Flow<Storage> {
        return storageDao.flowItem(getFullPath(shortPath).shortPath).mapNotNull { it }
            .onEach { storage ->
                storage.let { s ->
                    val updatedS = getSizeAndIsNew(s)
                    if (s.sizeFull != updatedS.sizeFull || s.sizeRead != updatedS.sizeRead) {
                        storageDao.update(updatedS)
                    }
                }
            }
    }

    private suspend fun getSizeAndIsNew(storage: Storage): Storage {
        val file = getFullPath(storage.path)
        getFromPath(file).let { manga ->
            storage.sizeFull = file.lengthMb
            storage.isNew = manga == null
            storage.sizeRead = manga?.let { it ->
                chapterDao.getItems(it.unic)
                    .asSequence()
                    .filter { it.isRead }
                    .sumOf { getFullPath(it.path).lengthMb }
            } ?: 0.0
        }
        return storage
    }

    private fun getFromPath(file: File): Manga? {
        return mangaDao.getItems().firstOrNull { getFullPath(it.path) == file }
    }
}

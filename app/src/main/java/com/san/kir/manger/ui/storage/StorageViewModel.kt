package com.san.kir.manger.ui.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb
import com.san.kir.manger.utils.extensions.shortPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.io.File

class StorageViewModel(app: Application) : AndroidViewModel(app) {
    private val storageDao = getDatabase(app).storageDao
    private val mangaDao = getDatabase(app).mangaDao
    private val chapterDao = getDatabase(app).chapterDao

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

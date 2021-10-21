package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb

class StorageRepository(context: Context) {
    private val db = getDatabase(context)
    private val mStorageDao = db.storageDao
    private val mMangaRepository = MangaRepository(context)
    private val mChapterDao = db.chapterDao

    suspend fun update(vararg storage: Storage) = mStorageDao.update(*storage)
    suspend fun insert(vararg storage: Storage) = mStorageDao.insert(*storage)
    suspend fun delete(vararg storage: Storage) = mStorageDao.delete(*storage)
    suspend fun items() = mStorageDao.items()

    suspend fun getSizeAndIsNew(storage: Storage): Storage {
        val file = getFullPath(storage.path)
        mMangaRepository.getFromPath(file).let { manga ->
            storage.sizeFull = file.lengthMb
            storage.isNew = manga == null
            storage.sizeRead = manga?.let { it ->
                mChapterDao.getItemsWhereManga(it.unic)
                    .asSequence()
                    .filter { it.isRead }
                    .sumOf { getFullPath(it.path).lengthMb }
            } ?: 0.0
        }
        return storage
    }

}

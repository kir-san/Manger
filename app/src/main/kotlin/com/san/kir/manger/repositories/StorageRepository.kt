package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.lengthMb
import com.san.kir.data.db.RoomDB
import com.san.kir.data.models.base.Storage

class StorageRepository(context: Context) {
    private val db = RoomDB.getDatabase(context)
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
                mChapterDao.getItemsWhereManga(it.name)
                    .asSequence()
                    .filter { it.isRead }
                    .sumOf { getFullPath(it.path).lengthMb }
            } ?: 0.0
        }
        return storage
    }

}

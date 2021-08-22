package com.san.kir.manger.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb
import com.san.kir.manger.utils.extensions.shortPath
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map

class StorageRepository(context: Context) {
    private val db = getDatabase(context)
    private val mStorageDao = db.storageDao
    private val mMangaRepository = MangaRepository(context)
    private val mChapterDao = db.chapterDao

    fun loadItems(): LiveData<List<Storage>> {
        return mStorageDao.loadItems()
    }

    fun loadItem(shortPath: String): LiveData<Storage?> {
        return mStorageDao.loadItem(shortPath)
    }

    suspend fun update(vararg storage: Storage) = mStorageDao.update(*storage)
    suspend fun insert(vararg storage: Storage) = mStorageDao.insert(*storage)
    suspend fun delete(vararg storage: Storage) = mStorageDao.delete(*storage)
    suspend fun items() = mStorageDao.items()

    fun flowItems() = mStorageDao.flowItems()

    suspend fun getSizeAndIsNew(storage: Storage): Storage {
        val file = getFullPath(storage.path)
        mMangaRepository.getFromPath(file).let { manga ->
            storage.sizeFull = file.lengthMb
            storage.isNew = manga == null
            storage.sizeRead = manga?.let { it ->
                mChapterDao.getItems(it.unic)
                    .asSequence()
                    .filter { it.isRead }
                    .sumOf { getFullPath(it.path).lengthMb }
            } ?: 0.0
        }
        return storage
    }

}

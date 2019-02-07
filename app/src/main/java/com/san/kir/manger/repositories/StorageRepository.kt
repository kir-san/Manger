package com.san.kir.manger.repositories

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import com.san.kir.manger.utils.shortPath
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class StorageRepository(context: Context) {
    private val db = getDatabase(context)
    private val mStorageDao = db.storageDao
    private val mMangaRepository = MangaRepository(context)
    private val mChapterDao = db.chapterDao

    fun pagedItems(): DataSource.Factory<Int, Storage> {
        return mStorageDao.pagedItems()
    }

    fun loadItems(): LiveData<List<Storage>> {
        return mStorageDao.loadItems()
    }

    fun getItems(): List<Storage> {
        return mStorageDao.getItems()
    }

    fun loadItem(shortPath: String): LiveData<Storage?> {
        return mStorageDao.loadItem(shortPath)
    }

    fun update(vararg storage: Storage) = GlobalScope.launch { mStorageDao.update(*storage) }
    fun insert(vararg storage: Storage) = GlobalScope.launch { mStorageDao.insert(*storage) }
    fun delete(vararg storage: Storage) = GlobalScope.launch { mStorageDao.delete(*storage) }

    fun loadPagedItems(): LiveData<PagedList<Storage>> {
        return LivePagedListBuilder(pagedItems(), 30).build()
    }

    fun loadAllSize(): LiveData<Double> {
        return Transformations.map(loadItems()) { list -> list.sumByDouble { it.sizeFull } }
    }

    fun loadItemWhere(shortPath: String): LiveData<Storage?> {
        return loadItem(getFullPath(shortPath).shortPath)
    }

    fun updateStorageItems() {
        if (asyncUpdates == null) {
            asyncUpdates = asyncUpdateStorageItems
        } else {
            asyncUpdates?.let {
                if (!it.isActive)
                    it.start()
            }
        }
    }

    fun getSizeAndIsNew(storage: Storage): Storage {
        val file = getFullPath(storage.path)
        mMangaRepository.getFromPath(file).let { manga ->
            storage.sizeFull = file.lengthMb
            storage.isNew = manga == null
            storage.sizeRead = manga?.let { it ->
                mChapterDao.getItems(it.unic)
                    .asSequence()
                    .filter { it.isRead }
                    .sumByDouble { getFullPath(it.path).lengthMb }
            } ?: 0.0
        }
        return storage
    }

    private val asyncUpdateStorageItems: Deferred<Any>
        get() = GlobalScope.async(Dispatchers.Default) {
            val list = getItems()
            val storageList = getFullPath(DIR.MANGA)
                .listFiles()
            if (list.isEmpty() || storageList.size != list.size) {
                storageList.forEach { dir ->
                    dir.listFiles().forEach { item ->
                        if (list.none { it.name == item.name }) {
                            insert(
                                Storage(
                                    name = item.name,
                                    path = item.shortPath,
                                    catalogName = dir.name
                                )
                            )
                        }
                    }
                }
            }
            list.onEach { update(getSizeAndIsNew(it)) }
        }

    private var asyncUpdates: Deferred<Any>? = null
}

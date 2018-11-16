package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import com.san.kir.manger.utils.shortPath
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@Dao
interface StorageDao : BaseDao<Storage> {
    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun pagedItems(): DataSource.Factory<Int, Storage>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun loadItems(): LiveData<List<Storage>>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun loadItem(shortPath: String): LiveData<Storage?>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun getItems(): List<Storage>
}

fun StorageDao.loadAllSize(): LiveData<Double> =
    Transformations.map(loadItems()) { list -> list.sumByDouble { it.sizeFull } }

fun StorageDao.loadPagedItems() =
    LivePagedListBuilder(pagedItems(), 20).build()

fun StorageDao.loadItemWhere(shortPath: String): LiveData<Storage?> {
    return loadItem(getFullPath(shortPath).shortPath)
}

fun StorageDao.updateStorageItems() {
    if (asyncUpdates == null) {
        asyncUpdates = asyncUpdateStorageItems
    } else {
        asyncUpdates?.let {
            if (!it.isActive)
                it.start()
        }
    }
}

fun Storage.getSizeAndIsNew(): Storage {
    val mangaDao = Main.db.mangaDao
    val chapters = Main.db.chapterDao

    val file = getFullPath(path)
    mangaDao.getFromPath(file).let { manga ->
        sizeFull = file.lengthMb
        isNew = manga == null
        sizeRead = manga?.let { it ->
            chapters.getItems(it.unic)
                .asSequence()
                .filter { it.isRead }
                .sumByDouble { getFullPath(it.path).lengthMb }
        } ?: 0.0
    }
    return this
}

private val StorageDao.asyncUpdateStorageItems: Deferred<Any>
    get() = GlobalScope.async(Dispatchers.Default) {
        val list = getItems()
        val storageList = getFullPath(DIR.MANGA).listFiles()
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
        list.onEach { update(it.getSizeAndIsNew()) }
    }

private var asyncUpdates: Deferred<Any>? = null



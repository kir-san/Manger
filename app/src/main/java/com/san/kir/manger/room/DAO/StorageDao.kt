package com.san.kir.manger.room.DAO

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getShortPath
import com.san.kir.manger.utils.lengthMb
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

@Dao
interface StorageDao : BaseDao<Storage> {
    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun loadPagedItems(): DataSource.Factory<Int, Storage>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun loadLivedItems(): LiveData<List<Storage>>

    @Query("SELECT * FROM StorageItem WHERE path IS :arg0")
    fun loadLivedItem(shortPath: String): LiveData<Storage?>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun simpleLoadItems(): List<Storage>
}

fun StorageDao.loadAllSize(): LiveData<Double> = Transformations.map(loadLivedItems()) { it.sumByDouble { it.sizeFull } }

fun StorageDao.loadPagedStorageItems() =
        LivePagedListBuilder(loadPagedItems(), 20).build()

fun StorageDao.loadLivedStorageItem(shortPath: String): LiveData<Storage?> {
    return loadLivedItem(getShortPath(getFullPath(shortPath)))
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

private val StorageDao.asyncUpdateStorageItems: Deferred<Any>
    get() = async {
        val list = simpleLoadItems()
        if (list.isEmpty()) {
            getFullPath(DIR.MANGA).listFiles().forEach { dir ->
                dir.listFiles().forEach { item ->
                    val storage = Storage(name = item.name,
                                          path = getShortPath(item),
                                          catalogName = dir.name).getSizeAndIsNew()
                    insert(storage)
                }
            }
        } else
            list.onEach { update(it.getSizeAndIsNew()) }
    }

private var asyncUpdates: Deferred<Any>? = null

fun Storage.getSizeAndIsNew(): Storage {
    val mangas = Main.db.mangaDao
    val chapters = Main.db.chapterDao

    val file = getFullPath(path)
    mangas.getFromPath(file).let {
        sizeFull = file.lengthMb
        isNew = it == null
        sizeRead = it?.let {
            chapters.loadChapters(it.unic)
                    .filter { it.isRead }
                    .sumByDouble { getFullPath(it.path).lengthMb }
        } ?: 0.0
    }
    return this
}

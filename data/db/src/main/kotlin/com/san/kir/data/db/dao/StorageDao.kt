package com.san.kir.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.base.getSizeAndIsNew
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao : BaseDao<Storage> {
    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun flowItems(): Flow<List<Storage>>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun allItemsBySizeFull(): PagingSource<Int, Storage>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun flowItem(shortPath: String): Flow<Storage?>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun item(shortPath: String): Storage?

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    suspend fun items(): List<Storage>
}

suspend fun StorageDao.searchNewItems(mangaDao: MangaDao, chapterDao: ChapterDao) {
    val list = items()
    getFullPath(DIR.MANGA).listFiles()?.let { storageList ->
        if (list.isEmpty() || storageList.size != list.size) {
            storageList.forEach { dir ->
                dir.listFiles()?.forEach { item ->
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
    }
    list.onEach { storage ->
        val file = getFullPath(storage.path)
        val manga = mangaDao.itemByPath(file)
        update(storage.getSizeAndIsNew(
            file,
            manga,
            manga?.let {chapterDao.getItemsWhereManga(it.name) })
        )
    }
}

package com.san.kir.storage.logic.repo

import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.db.dao.itemByPath
import com.san.kir.data.models.base.Storage
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val storageDao: StorageDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) {
    val items = storageDao.loadItems()
    val fullSize = storageDao.loadFullSize()

    suspend fun mangaFromPath(path: String) = mangaDao.itemByPath(getFullPath(path))
    fun storageFromFile(path: String) = storageDao.loadItemByPath(path)

    suspend fun delete(item: Storage) = withDefaultContext {
        kotlin.runCatching {
            storageDao.delete(item)
            getFullPath(item.path).deleteRecursively()
        }/*.onFailure {
            context.longToast(it.toString())
        }*/
    }

    fun loadManga(mangaId: Long) = mangaDao.loadItemById(mangaId)
}

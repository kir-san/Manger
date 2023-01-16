package com.san.kir.chapters.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChaptersRepository @Inject constructor(
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) {

    val items = chapterDao.loadItemsByNotStatus().map { it.sortedBy { c -> c.status.ordinal } }
    fun items(mangaId: Long) = chapterDao.loadSimpleItemsByMangaId(mangaId)
    fun loadManga(mangaId: Long) = mangaDao.loadItemById(mangaId)
    suspend fun update(manga: Manga) = withIoContext { mangaDao.update(manga) }
    suspend fun update(chapter: Chapter) = withIoContext { chapterDao.update(chapter) }
    suspend fun update(ids: List<Long>, isRead: Boolean) =
        withIoContext { chapterDao.updateIsRead(ids, isRead) }
    suspend fun clear(ids: List<Long>) = withIoContext { chapterDao.updateStatus(ids) }

    suspend fun item(id: Long) = withIoContext { chapterDao.itemById(id) }
    suspend fun delete(items: List<Long>) = withIoContext { chapterDao.deleteByIds(items) }

    suspend fun allItems(mangaId: Long) = withIoContext {
        chapterDao.itemsByMangaId(mangaId)
    }

    suspend fun notReadItems(mangaId: Long) = withIoContext {
        chapterDao.itemsNotReadByMangaId(mangaId)
    }

    suspend fun newItem(mangaId: Long) = withIoContext {
        chapterDao.itemsNotReadByMangaId(mangaId).firstOrNull()
    }
}

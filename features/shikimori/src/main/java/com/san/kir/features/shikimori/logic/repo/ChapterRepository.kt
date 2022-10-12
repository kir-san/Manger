package com.san.kir.features.shikimori.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.models.base.Chapter
import javax.inject.Inject

class ChapterRepository @Inject constructor(
    private val chapterDao: ChapterDao
) {

    suspend fun itemsByMangaId(mangaId: Long) = withIoContext {
        chapterDao.itemsByMangaId(mangaId)
    }

    suspend fun update(items: List<Chapter>) = withIoContext {
        chapterDao.update(items)
    }
}

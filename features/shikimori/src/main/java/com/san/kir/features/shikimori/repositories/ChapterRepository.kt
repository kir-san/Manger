package com.san.kir.features.shikimori.repositories

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.models.base.Chapter
import javax.inject.Inject

class ChapterRepository @Inject constructor(
    private val chapterDao: ChapterDao
) {

    suspend fun itemsByManga(name: String) = withIoContext {
        chapterDao.getItemsWhereManga(name)
    }

    suspend fun update(items: List<Chapter>) = withIoContext {
        chapterDao.update(items)
    }
}

package com.san.kir.chapters.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import javax.inject.Inject

class LatestRepository @Inject constructor(
    private val chapterDao: ChapterDao
) {
    val items = chapterDao.loadSimpleItems()
    val notReadItems = chapterDao.loadNotReadItems()

    suspend fun update(items: List<Long>, isInUpdate: Boolean) =
        withIoContext { chapterDao.update(items, isInUpdate) }
}

package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) {
    suspend fun manga(mangaId: Long) = withIoContext { mangaDao.itemById(mangaId) }
    suspend fun add(chapter: Chapter) = withIoContext { chapterDao.insert(chapter) }
    suspend fun add(chapters: List<Chapter>) = withIoContext { chapterDao.insert(chapters) }
    suspend fun chapters(manga: Manga) = withIoContext { chapterDao.itemsByMangaId(manga.id) }
    suspend fun update(chapter: Chapter) = withIoContext { chapterDao.update(chapter) }
    suspend fun update(chapters: List<Chapter>) = withIoContext { chapterDao.update(chapters) }
    suspend fun delete(chapters: List<Chapter>) = withIoContext { chapterDao.delete(chapters) }
    suspend fun deleteByIds(chapters: List<Long>) =
        withIoContext { chapterDao.deleteByIds(chapters) }
}

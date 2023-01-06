package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.parsing.SiteCatalogsManager
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val manager: SiteCatalogsManager,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) {

    suspend fun manga(mangaId: Long) = withIoContext { mangaDao.itemById(mangaId) }
    suspend fun update(chapter: Chapter) = withIoContext { chapterDao.update(chapter) }
    suspend fun update(chapters: List<Chapter>) = withIoContext { chapterDao.update(chapters) }
    suspend fun add(chapter: Chapter) = withIoContext { chapterDao.insert(chapter) }
    suspend fun chapters(manga: Manga) = withIoContext {
        chapterDao.itemsByMangaId(manga.id)
            .apply { updatePages(manga) }
    }

    /* Обновление страниц в главах */
    private suspend fun List<Chapter>.updatePages(manga: Manga) {
        kotlin.runCatching {
            // Отфильтровываем те в которых, либо нет страниц, либо не все страницы
            // либо это альтернативный сайт
            filter {
                manga.isAlternativeSite
                        || it.pages.isEmpty()
                        || it.pages.any { chap -> chap.isBlank() }
            }
                // Получаем список страниц и сохраняем
                .map { it.copy(pages = manager.pages(it)) }
                .apply { chapterDao.update(*toTypedArray()) }
        }.onFailure { it.printStackTrace() }
    }
}

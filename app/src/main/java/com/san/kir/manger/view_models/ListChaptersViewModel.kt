package com.san.kir.manger.view_models

import android.app.Application
import com.san.kir.manger.components.list_chapters.ChapterComparator
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga

class ListChaptersViewModel(app: Application) {
    private val mChapterRepository = ChapterRepository(app)
    private val mMangaRepository = MangaRepository(app)

    suspend fun update(vararg chapter: Chapter) = mChapterRepository.update(*chapter)
    suspend fun update(vararg manga: Manga) = mMangaRepository.update(*manga)
    suspend fun delete(vararg chapter: Chapter) = mChapterRepository.delete(*chapter)

    // TODO не удалять пока не добавлю в просмоторщик
    suspend fun getFirstChapter(manga: Manga): Chapter {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        if (manga.isAlternativeSort) {
            list = list.sortedWith(ChapterComparator())
        }

        val chapter = list.first()
        chapter.progress = 0
        update(chapter)

        return list.first()
    }
}

package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.san.kir.ankofork.Binder
import com.san.kir.manger.components.list_chapters.ChapterComparator
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.enums.ChapterFilter

class ListChaptersViewModel(app: Application) : AndroidViewModel(app) {
    private val mChapterRepository = ChapterRepository(app)
    private val mMangaRepository = MangaRepository(app)
    private val mDownloadRepository = DownloadRepository(app)

    val filter = Binder(ChapterFilter.ALL_READ_ASC)

    val isAction = Binder(false)
    val isUpdate = Binder(true)
    val isVisibleBottom = Binder(true) // Отображение бара снизу

    suspend fun chapters(manga: Manga) = mChapterRepository.getItems(manga.unic)
    suspend fun chaptersAsc(manga: Manga) = mChapterRepository.getItemsAsc(manga.unic)
    suspend fun chaptersNotReadAsc(manga: Manga) = mChapterRepository.getItemsNotReadAsc(manga.unic)

    suspend fun manga(mangaUnic: String): Manga {
        val manga = mMangaRepository.getItem(mangaUnic)
        manga.populate += 1
        mMangaRepository.update(manga)
        return manga
    }

    suspend fun update(vararg chapter: Chapter) = mChapterRepository.update(*chapter)
    suspend fun update(vararg manga: Manga) = mMangaRepository.update(*manga)
    suspend fun delete(vararg chapter: Chapter) = mChapterRepository.delete(*chapter)

    fun getDownloadItem(item: Chapter): LiveData<DownloadItem?> {
        return mDownloadRepository.loadItem(item.site)
    }

    override fun onCleared() {
        isAction.close()
        isUpdate.close()
    }

    suspend fun getFirstNotReadChapter(manga: Manga): Chapter? {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        list = if (manga.isAlternativeSort) {
            try {
                list.sortedWith(ChapterComparator())
            } catch (e: Exception) {
                list
            }
        } else {
            list
        }

        return list.firstOrNull { !it.isRead }
    }

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

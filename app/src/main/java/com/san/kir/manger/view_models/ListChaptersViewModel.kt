package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.san.kir.manger.components.list_chapters.ChapterComparator
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.Filter
import com.san.kir.manger.utils.enums.FilterAsc
import com.san.kir.manger.utils.enums.FilterDesc

class ListChaptersViewModel(app: Application) : AndroidViewModel(app) {
    private val mChapterRepository = ChapterRepository(app)
    private val mMangaRepository = MangaRepository(app)
    private val mDownloadRepository = DownloadRepository(app)

    val isAction = Binder(false)
    val isUpdate = Binder(true)
    val isVisibleBottom = Binder(true) // Отображение бара снизу
    val sortIndicator = Binder(false) // Индикатор сортировки
    val filterIndicator = Binder(ChapterFilter.ALL_READ_ASC) {
        sortIndicator.item = filterStateHelp.isAsc
    }

    var filterStateHelp: Filter = FilterAsc
    var filterState: String
        get() = filterIndicator.item.name
        set(value) {
            val state = ChapterFilter.valueOf(value)
            filterStateHelp = when (state) {
                ChapterFilter.ALL_READ_ASC,
                ChapterFilter.NOT_READ_ASC,
                ChapterFilter.IS_READ_ASC -> {
                    FilterAsc
                }
                ChapterFilter.ALL_READ_DESC,
                ChapterFilter.NOT_READ_DESC,
                ChapterFilter.IS_READ_DESC -> {
                    FilterDesc
                }
            }
            sortIndicator.item = filterStateHelp.isAsc
            filterIndicator.item = state
        }

    fun getChapters(manga: Manga): List<Chapter> {
        return mChapterRepository.getItems(manga.unic)
    }

    fun getChaptersNotReadAsc(manga: Manga): List<Chapter> {
        return mChapterRepository.getItemsNotReadAsc(manga.unic)
    }

    fun getChaptersAsc(manga: Manga): List<Chapter> {
        return mChapterRepository.getItemsAsc(manga.unic)
    }

    fun getManga(mangaUnic: String): Manga {
        return mMangaRepository.getItem(mangaUnic)
    }

    fun updateChapter(vararg chapter: Chapter) = mChapterRepository.update(*chapter)
    fun deleteChapter(vararg chapter: Chapter) = mChapterRepository.delete(*chapter)
    fun updateManga(vararg manga: Manga) = mMangaRepository.update(*manga)

    fun getDownloadItem(item: Chapter): LiveData<DownloadItem?> {
        return mDownloadRepository.loadItem(item.site)
    }

    fun toggleFilterInverse() {
        filterStateHelp = filterStateHelp.reverse()
        filterIndicator.item = filterIndicator.item.inverse()
    }

    override fun onCleared() {
        isAction.close()
        isUpdate.close()
        filterIndicator.close()
        sortIndicator.close()
    }

    fun getFirstNotReadChapter(manga: Manga): Chapter? {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        list = if (manga.isAlternativeSort) {
            list.sortedWith(ChapterComparator())
        } else {
            list
        }

        return list.firstOrNull { !it.isRead }
    }

    fun getFirstChapter(manga: Manga): Chapter {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        if (manga.isAlternativeSort) {
            list = list.sortedWith(ChapterComparator())
        }

        val chapter = list.first()
        chapter.progress = 0
        updateChapter(chapter)

        return list.first()
    }
}

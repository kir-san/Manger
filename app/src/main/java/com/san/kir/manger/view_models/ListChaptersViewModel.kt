package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga

class ListChaptersViewModel(app: Application) : AndroidViewModel(app) {
    private val mChapterRepository = ChapterRepository(app)
    private val mMangaRepository = MangaRepository(app)
    private val mDownloadRepository = DownloadRepository(app)

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
}

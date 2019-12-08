package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.room.entities.Chapter

class LatestChapterViewModel(app: Application) : AndroidViewModel(app) {
    private val mChapterRepository = ChapterRepository(app)
    private val mDownloadRepository = DownloadRepository(app)

    fun getLatestItems() = mChapterRepository.loadInUpdateItems()
    suspend fun delete(chapter: Chapter) = mChapterRepository.delete(chapter)
    suspend fun update(chapter: Chapter) = mChapterRepository.update(chapter)
    suspend fun hasNewChapters() = newChapters().isNotEmpty()
    suspend fun newChapters() = mChapterRepository.newChapters()

    fun getDownloadItems(item: Chapter) = mDownloadRepository.loadItem(item.site)
}


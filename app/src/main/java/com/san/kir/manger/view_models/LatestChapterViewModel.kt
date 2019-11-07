package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.utils.enums.ChapterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LatestChapterViewModel(app: Application) : AndroidViewModel(app) {
    private val mChapterRepository = ChapterRepository(app)
    private val mDownloadRepository = DownloadRepository(app)

    fun getLatestItems() = mChapterRepository.loadInUpdateItems()
    suspend fun delete(chapter: Chapter) = mChapterRepository.delete(chapter)
    suspend fun hasNewChapters() = newChapters().isNotEmpty()
    suspend fun newChapters() = mChapterRepository.newChapters()

    fun clearAll(): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            mChapterRepository.update(
                *mChapterRepository
                    .getItems()
                    .filter { it.isInUpdate }
                    .onEach { it.isInUpdate = false }
                    .toTypedArray()
            )
        }
    }

    fun clearRead(): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            mChapterRepository.update(
                *mChapterRepository
                    .getItems()
                    .filter { it.isInUpdate }
                    .filter { it.isRead }
                    .onEach { it.isInUpdate = false }
                    .toTypedArray()
            )
        }
    }

    fun clearDownloaded(): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            mChapterRepository.update(
                *mChapterRepository
                    .getItems()
                    .filter { it.isInUpdate }
                    .filter { it.action == ChapterStatus.DELETE }
                    .onEach { it.isInUpdate = false }
                    .toTypedArray()
            )
        }
    }

    fun getDownloadItems(item: Chapter) = mDownloadRepository.loadItem(item.site)
}


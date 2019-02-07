package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.LatestChapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class LatestChapterViewModel(app: Application) : AndroidViewModel(app), CoroutineScope {
    private val mDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val mJob = Job()

    override val coroutineContext = mDispatcher + mJob

    private val mLatestChapterRepository = LatestChapterRepository(app)
    private val mDownloadRepository = DownloadRepository(app)

    fun getLatestItems(): LiveData<List<LatestChapter>> {
        return mLatestChapterRepository.loadItems()
    }

    fun latestDelete(latestChapter: LatestChapter) {
        mLatestChapterRepository.delete(latestChapter)
    }

    fun latestHasNewChapters(): Boolean {
        return mLatestChapterRepository.hasNewChapters()
    }

    fun getLatestNewChapters(): List<LatestChapter> {
        return mLatestChapterRepository.getNewChapters()
    }

    fun latestClearAll(): Job {
        return launchCtx { mLatestChapterRepository.clearAll() }
    }

    fun latestClearRead(): Job {
        return launchCtx {
            with(mLatestChapterRepository) {
                getItems()
                    .map { launch { if (isRead(it)) delete(it) } }
                    .joinAll()
            }
        }
    }

    fun latestClearDownloaded(): Job {
        return launchCtx { mLatestChapterRepository.clearDownloaded() }
    }

    fun getDownloadItems(item: LatestChapter): LiveData<DownloadItem?> {
        return mDownloadRepository.loadItem(item.site)
    }

    fun isChapterRead(item: LatestChapter): Boolean {
        return mLatestChapterRepository.isRead(item)
    }

    override fun onCleared() {
        super.onCleared()
        mJob.cancel()
    }
}


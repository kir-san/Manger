package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.room.models.Manga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class DownloadManagerViewModel(app: Application) : AndroidViewModel(app), CoroutineScope {
    private val mDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val mJob = Job()

    override val coroutineContext = mDispatcher + mJob

    private val mDownloaRepository = DownloadRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getDownloadItems(): LiveData<List<DownloadItem>> {
        return mDownloaRepository.loadItems()
    }

    fun downloadDelete(downloadItem: DownloadItem) {
        mDownloaRepository.delete(downloadItem)
    }

    fun getMangaItemOrNull(item: DownloadItem): Manga? {
        return mMangaRepository.getItemOrNull(item.manga)
    }

    fun clearCompletedDownloads() {
        launchCtx {
            val items = mDownloaRepository
                .getItems()
                .filter { it.status == DownloadStatus.completed }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mJob.cancel()
    }

    fun clearPausedDownloads() {
        launchCtx {
            val items = mDownloaRepository
                .getItems()
                .filter { it.status == DownloadStatus.pause }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }

    fun clearErrorDownloads() {
        launchCtx {
            val items = mDownloaRepository
                .getItems()
                .filter { it.status == DownloadStatus.error }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }

    fun clearAllDownloads() {
        launchCtx {
            val items = mDownloaRepository
                .getItems()
                .filter {
                    it.status == DownloadStatus.completed
                            || it.status == DownloadStatus.pause
                            || it.status == DownloadStatus.error
                }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }
}

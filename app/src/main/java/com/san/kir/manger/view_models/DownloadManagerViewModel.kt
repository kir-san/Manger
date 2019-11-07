package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.enums.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadManagerViewModel(app: Application) : AndroidViewModel(app) {
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
        viewModelScope.launch(Dispatchers.Default) {
            val items = mDownloaRepository
                .getItems()
                .filter { it.status == DownloadStatus.completed }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }

    fun clearPausedDownloads() {
        viewModelScope.launch(Dispatchers.Default) {
            val items = mDownloaRepository
                .getItems()
                .filter { it.status == DownloadStatus.pause && !it.isError }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }

    fun clearErrorDownloads() {
        viewModelScope.launch(Dispatchers.Default) {
            val items = mDownloaRepository
                .getItems()
                .filter { it.status == DownloadStatus.pause && it.isError }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch(Dispatchers.Default) {
            val items = mDownloaRepository
                .getItems()
                .filter {
                    it.status == DownloadStatus.completed
                            || it.status == DownloadStatus.pause
                }
                .toTypedArray()
            mDownloaRepository.delete(*items)
        }
    }
}

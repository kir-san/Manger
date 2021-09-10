package com.san.kir.manger.components.download_manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.enums.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadManagerViewModel(app: Application) : AndroidViewModel(app) {
    private val mDownloaRepository = DownloadRepository(app)
    private val mMangaRepository = MangaRepository(app)

    suspend fun getItems() = mDownloaRepository.items()
    fun getDownloadItems() = mDownloaRepository.loadItems()
    fun loadItems(link: String) = mDownloaRepository.loadItem(link)

    suspend fun delete(downloadItem: DownloadItem) = mDownloaRepository.delete(downloadItem)
    suspend fun getMangaItemOrNull(item: DownloadItem) = mMangaRepository.getItemOrNull(item.manga)

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

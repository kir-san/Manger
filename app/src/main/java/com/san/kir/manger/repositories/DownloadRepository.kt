package com.san.kir.manger.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.getDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DownloadRepository(context: Context) {
    private val db = getDatabase(context)
    private val mDownloadDao = db.downloadDao

    fun loadItems(): LiveData<List<DownloadItem>> {
        return mDownloadDao.loadItems()
    }

    fun getItems(status: Int): List<DownloadItem> {
        return mDownloadDao.getItems(status)
    }

    fun loadItems(status1: Int, status2: Int): LiveData<List<DownloadItem>> {
        return mDownloadDao.loadItems(status1, status2)
    }

    fun getItems(): List<DownloadItem> {
        return mDownloadDao.getItems()
    }

    fun getItem(link: String): DownloadItem? {
        return mDownloadDao.getItem(link)
    }

    fun loadItem(link: String): LiveData<DownloadItem?> {
        return mDownloadDao.loadItem(link)
    }

    fun insert(vararg downloadItem: DownloadItem) = GlobalScope.launch { mDownloadDao.insert(*downloadItem) }
    fun update(vararg downloadItem: DownloadItem) = GlobalScope.launch { mDownloadDao.update(*downloadItem) }
    fun delete(vararg downloadItem: DownloadItem) = GlobalScope.launch { mDownloadDao.delete(*downloadItem) }
}


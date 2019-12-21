package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.getDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class DownloadRepository(context: Context) {
    private val db = getDatabase(context)
    private val mDownloadDao = db.downloadDao

    suspend fun items() = mDownloadDao.items()
    fun loadItems() = mDownloadDao.loadItems()

    suspend fun getItems(status: Int) = mDownloadDao.getItems(status)

    fun loadItems(status1: Int, status2: Int) = mDownloadDao.loadItems(status1, status2)

    suspend fun getItems() = mDownloadDao.getItems()

    suspend fun getItem(link: String) = mDownloadDao.getItem(link)

    fun loadItem(link: String): Flow<DownloadItem> {
        var lastObj: DownloadItem? = null
        return mDownloadDao.loadItem(link)
            .filter {
                if (lastObj == null || lastObj != it) {
                    lastObj = it
                    true
                } else {
                    false
                }
            }
    }

    suspend fun insert(vararg downloadItem: DownloadItem) = mDownloadDao.insert(*downloadItem)
    suspend fun update(vararg downloadItem: DownloadItem) = mDownloadDao.update(*downloadItem)
    suspend fun delete(vararg downloadItem: DownloadItem) = mDownloadDao.delete(*downloadItem)
}


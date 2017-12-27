package com.san.kir.manger.room.DAO

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.DownloadItem

@Dao
interface DownloadDao : BaseDao<DownloadItem> {
    @Query("SELECT * FROM downloads WHERE status IS 1")
    fun loadLoadingDownloads(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status IS 2")
    fun loadPauseDownloads(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status IS 3")
    fun loadErrorDownloads(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status IS 4")
    fun loadCompleteDownloads(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM downloads")
    fun loadItems(): List<DownloadItem>

    @Query("SELECT * FROM downloads WHERE link IS :arg0")
    fun loadItem(link: String): DownloadItem?

    @Query("SELECT * FROM downloads WHERE link IS :arg0")
    fun loadLivedItem(link: String): LiveData<DownloadItem?>
}


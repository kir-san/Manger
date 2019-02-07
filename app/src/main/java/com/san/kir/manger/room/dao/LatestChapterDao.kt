package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.LatestChapter

@Dao
interface LatestChapterDao : BaseDao<LatestChapter> {
    @Query("SELECT * FROM latestChapters ORDER BY id DESC")
    fun loadItems(): LiveData<List<LatestChapter>>

    @Query("SELECT * FROM latestChapters")
    fun getItems(): List<LatestChapter>

    @Query("SELECT * FROM latestChapters WHERE site IS :link")
    fun getItemsWhereLink(link: String): List<LatestChapter>

    @Query("SELECT * FROM latestChapters WHERE manga IS :manga")
    fun getItemsWhereManga(manga: String): List<LatestChapter>

    @Query("DELETE FROM latestChapters")
    fun deleteAll()
}

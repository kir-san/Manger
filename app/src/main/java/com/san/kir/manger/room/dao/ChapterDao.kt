package com.san.kir.manger.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.Chapter

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM chapters")
    suspend fun getItems(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :manga")
    suspend fun getItems(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE site IS :site")
    suspend fun getItem(site: String): Chapter?

    @Query("SELECT * FROM chapters WHERE manga IS :manga ORDER BY id ASC")
    suspend fun getItemsAsc(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :manga AND isRead IS 0 ORDER BY id ASC")
    suspend fun getItemsNotReadAsc(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE isInUpdate IS 1 ORDER BY id DESC")
    fun loadInUpdateItems(): LiveData<List<Chapter>>
}


package com.san.kir.manger.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Chapter

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM chapters WHERE manga IS :manga")
    fun getItems(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE site IS :site")
    fun getItem(site: String): Chapter?

    @Query("SELECT * FROM chapters WHERE manga IS :manga ORDER BY id ASC")
    fun getItemsAsc(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :manga AND isRead IS 0 ORDER BY id ASC")
    fun getItemsNotReadAsc(manga: String): List<Chapter>
}


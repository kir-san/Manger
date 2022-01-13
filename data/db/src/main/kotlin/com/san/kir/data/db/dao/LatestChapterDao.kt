package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.LatestChapter

@Dao
interface LatestChapterDao : BaseDao<LatestChapter> {
    @Query("SELECT * FROM latestChapters")
    suspend fun getItems(): List<LatestChapter>

    @Query("DELETE FROM latestChapters")
    suspend fun deleteAll()
}

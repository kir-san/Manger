package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.ChapterTask
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterTaskDao : BaseDao<ChapterTask> {

    @Query("SELECT * FROM chapter_task")
    fun loadItems(): Flow<List<ChapterTask>>

    @Query("SELECT * FROM chapter_task WHERE chapter_id=:id")
    fun loadItemByChapterId(id: Long): Flow<ChapterTask?>

    @Query("SELECT * FROM chapter_task WHERE chapter_id=:id")
    fun itemByChapterId(id: Long): ChapterTask?

    @Query("DELETE FROM chapter_task WHERE id=:id")
    fun removeById(id: Long)

    @Query("DELETE FROM chapter_task WHERE id IN (:ids)")
    fun removeByIds(ids: List<Long>)

    @Query("DELETE FROM chapter_task")
    fun clear()
}

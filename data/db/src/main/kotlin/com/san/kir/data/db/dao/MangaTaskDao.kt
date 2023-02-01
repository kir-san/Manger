package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.MangaTask
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaTaskDao : BaseDao<MangaTask> {

    @Query("SELECT * FROM manga_task")
    fun loadItems(): Flow<List<MangaTask>>

    @Query("SELECT * FROM manga_task WHERE manga_id=:id")
    fun loadItemByMangaId(id: Long): Flow<MangaTask?>

    @Query("SELECT * FROM manga_task WHERE manga_id=:id")
    fun itemByMangaId(id: Long): MangaTask?

    @Query("DELETE FROM manga_task WHERE id=:id")
    fun removeById(id: Long)

    @Query("DELETE FROM manga_task WHERE id IN (:ids)")
    fun removeByIds(ids: List<Long>)

    @Query("DELETE FROM manga_task")
    fun clear()
}

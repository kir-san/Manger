package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface ShikimoriDao : BaseDao<ShikiDbManga> {
    @Query("SELECT * FROM shikimori")
    fun loadItems(): Flow<List<ShikiDbManga>>

    @Query("SELECT * FROM shikimori WHERE id IS :targetID")
    suspend fun itemByTargetId(targetID: Long): ShikiDbManga?

    @Query("SELECT * FROM shikimori WHERE id IS :targetID")
    fun loadItemByTargetId(targetID: Long): Flow<ShikiDbManga?>

    @Query("SELECT * FROM shikimori WHERE lid_id IS :libId")
    suspend fun itemByLibId(libId: Long): ShikiDbManga?

    @Query("SELECT * FROM shikimori WHERE lid_id IS :libId")
    fun loadItemByLibId(libId: Long): Flow<ShikiDbManga?>

    @Query("DELETE FROM shikimori")
    suspend fun clearAll()

    @Query("SELECT * FROM libarary_manga ORDER BY name")
    fun loadLibraryItems(): Flow<List<SimplifiedMangaWithChapterCounts>>

    @Query("SELECT * FROM libarary_manga WHERE id IS :id")
    fun loadLibraryItemById(id: Long): Flow<SimplifiedMangaWithChapterCounts>

    @Query("DELETE FROM shikimori WHERE id IS :targetID")
    suspend fun removeByTargetId(targetID: Long)

    @Query("UPDATE shikimori SET lid_id = :libId WHERE id IS :targetID")
    suspend fun updateLibIdByTargetId(targetID: Long, libId: Long)
}


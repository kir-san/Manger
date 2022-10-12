package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.extend.SimplifiedChapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM simple_chapter")
    fun loadSimpleItems(): Flow<List<SimplifiedChapter>>

    @Query("SELECT * FROM chapters WHERE isInUpdate=1 AND isRead=0")
    fun loadNotReadItems(): Flow<List<Chapter>>

    @Query("SELECT COUNT(id) FROM chapters WHERE isInUpdate=1")
    fun loadLatestCount(): Flow<Int>

    @Query("SELECT COUNT(id) FROM chapters WHERE status='QUEUED' OR status='LOADING'")
    fun loadDownloadCount(): Flow<Int>

    @Query("SELECT * FROM chapters WHERE manga_id IS :mangaId")
    fun loadItemsByMangaId(mangaId: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE status IS NOT :status ORDER BY status,ordering")
    fun loadItemsByNotStatus(status: DownloadState = DownloadState.UNKNOWN): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters")
    suspend fun items(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE error IS 0 ORDER BY ordering")
    suspend fun itemsByError(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :mangaId")
    suspend fun itemsByMangaId(mangaId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE status IS :status ORDER BY ordering")
    suspend fun itemsByStatus(status: DownloadState): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :mangaId AND isRead IS 0 ORDER BY id ASC")
    suspend fun itemsNotReadByMangaId(mangaId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE id IS :id")
    suspend fun itemById(id: Long): Chapter

    @Query("SELECT manga_id FROM chapters WHERE id IS :id")
    suspend fun mangaIdById(id: Long): Long

    @Query("UPDATE chapters SET isInUpdate=:isInUpdate WHERE id IN (:ids)")
    suspend fun update(ids: List<Long>, isInUpdate: Boolean)
}


package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.extend.DownloadChapter
import com.san.kir.data.models.extend.SimplifiedChapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM simple_chapter")
    fun loadSimpleItems(): Flow<List<SimplifiedChapter>>

    @Query(
        "SELECT id, status, " +
                "IIF(totalPages=0, totalPages,  downloadPages * 100/ chapters.totalPages) AS download_progress, " +
                "progress, isRead, pages, name, '' AS manga, date, path " +
                "FROM chapters WHERE manga_id=:mangaId"
    )
    fun loadSimpleItemsByMangaId(mangaId: Long): Flow<List<SimplifiedChapter>>

    @Query("SELECT * FROM chapters WHERE isInUpdate=1 AND isRead=0")
    fun loadNotReadItems(): Flow<List<Chapter>>

    @Query("SELECT COUNT(id) FROM simple_chapter")
    fun loadLatestCount(): Flow<Int>

    @Query("SELECT COUNT(id) FROM chapters WHERE status='QUEUED' OR status='LOADING'")
    fun loadDownloadCount(): Flow<Int>

    @Query(
        "SELECT chapters.id, chapters.name, manga.name AS manga, manga.logo AS logo, " +
                "chapters.status, chapters.totalTime, chapters.downloadSize, chapters.downloadPages, " +
                "chapters.pages, chapters.error " +
                "FROM chapters JOIN manga ON chapters.manga_id=manga.id " +
                "WHERE chapters.status IS NOT :status " +
                "ORDER BY chapters.status,chapters.ordering"
    )
    fun loadItemsByNotStatus(status: DownloadState = DownloadState.UNKNOWN): Flow<List<DownloadChapter>>

    @Query("SELECT * FROM chapters")
    suspend fun items(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE error IS 0 ORDER BY ordering")
    suspend fun itemsByError(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga_id IS :mangaId")
    suspend fun itemsByMangaId(mangaId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE status IS :status ORDER BY ordering")
    suspend fun itemsByStatus(status: DownloadState): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga_id IS :mangaId AND isRead IS 0 ORDER BY id ASC")
    suspend fun itemsNotReadByMangaId(mangaId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE id IS :id")
    suspend fun itemById(id: Long): Chapter

    @Query("SELECT manga_id FROM chapters WHERE id IS :id")
    suspend fun mangaIdById(id: Long): Long

    @Query("UPDATE chapters SET isInUpdate=:isInUpdate WHERE id IN (:ids)")
    suspend fun updateIsInUpdate(ids: List<Long>, isInUpdate: Boolean)

    @Query("UPDATE chapters SET isRead=:readStatus WHERE id IN (:ids)")
    suspend fun updateIsRead(ids: List<Long>, readStatus: Boolean)

    @Query("UPDATE chapters SET status=:status WHERE id IN (:ids)")
    suspend fun updateStatus(ids: List<Long>, status: DownloadState = DownloadState.UNKNOWN)

    @Query("UPDATE chapters SET status=:status, ordering=:time WHERE id IS :id")
    suspend fun setQueueStatus(
        id: Long,
        status: DownloadState = DownloadState.QUEUED,
        time: Long = System.currentTimeMillis(),
    )

    @Query("DELETE FROM chapters WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}


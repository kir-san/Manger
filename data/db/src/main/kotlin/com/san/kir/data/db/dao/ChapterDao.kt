package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.base.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM chapters")
    suspend fun items(): List<Chapter>

    @Query(
        "SELECT manga FROM chapters " +
                "WHERE ${Chapter.Col.id} IS :chapterID"
    )
    suspend fun getMangaName(chapterID: Long): String

    @Query(
        "SELECT * FROM chapters " +
                "WHERE ${Chapter.Col.id} IS :chapterID"
    )
    suspend fun itemWhereId(chapterID: Long): Chapter

    @Query(
        "SELECT * FROM chapters " +
                "WHERE manga IS :manga"
    )
    suspend fun getItemsWhereManga(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga_id IS :mangaId")
    suspend fun itemsWhereMangaId(mangaId: Long): List<Chapter>

    @Query(
        "SELECT * FROM chapters " +
                "WHERE manga IS :manga"
    )
    fun loadItemsWhereManga(manga: String): Flow<List<Chapter>>

    @Query(
        "SELECT COUNT(*) FROM chapters " +
                "WHERE manga IS :manga " +
                "AND ${Chapter.Col.isRead} IS 0"
    )
    fun loadCountNotReadItemsWhereManga(manga: String): Flow<Int>

    @Query(
        "SELECT * FROM chapters " +
                "WHERE ${Chapter.Col.link} IS :link"
    )
    suspend fun getItemWhereLink(link: String): Chapter?

    @Query(
        "SELECT * FROM chapters " +
                "WHERE ${Chapter.Col.status} IS :status " +
                "ORDER BY `${Chapter.Col.order}`"
    )
    suspend fun getItemsWhereStatus(status: DownloadState): List<Chapter>

    @Query(
        "SELECT * FROM chapters " +
                "WHERE ${Chapter.Col.error} IS 0 " +
                "ORDER BY `${Chapter.Col.order}`"
    )
    suspend fun getErrorItems(): List<Chapter>

    @Query(
        "SELECT * FROM chapters " +
                "WHERE manga IS :manga " +
                "AND ${Chapter.Col.isRead} IS 0 " +
                "ORDER BY ${Chapter.Col.id} ASC"
    )
    suspend fun getItemsNotReadAsc(manga: String): List<Chapter>

    @Query("SELECT COUNT(id) FROM chapters WHERE status='QUEUED' OR status='LOADING'")
    fun loadDownloadCount(): Flow<Int>

    @Query("SELECT COUNT(id) FROM chapters WHERE isInUpdate=1")
    fun loadAllItemsCount(): Flow<Int>

    @Query(
        "SELECT * FROM chapters " +
                "WHERE ${Chapter.Col.isInUpdate} IS 1 " +
                "ORDER BY ${Chapter.Col.id} DESC"
    )
    fun loadAllItems(): Flow<List<Chapter>>

    @Query(
        "SELECT * FROM chapters " +
                "WHERE `${Chapter.Col.status}` IS NOT :status " +
                "ORDER BY ${Chapter.Col.status},`${Chapter.Col.order}`"
    )
    fun loadDownloadItemsWhereStatusNot(status: DownloadState = DownloadState.UNKNOWN): Flow<List<Chapter>>
}


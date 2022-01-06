package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.Manga
import com.san.kir.data.models.Shikimori
import com.san.kir.data.models.SimpleManga
import com.san.kir.data.models.columns.MangaColumn
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface MangaDao : BaseDao<Manga> {
    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    suspend fun getItems(): List<Manga>

    @Query("SELECT * FROM ${SimpleManga.viewName}")
    suspend fun simpleItems(): List<SimpleManga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` " +
            "WHERE `${MangaColumn.name}` IS :name")
    suspend fun item(name: String): Manga

    @Query("SELECT * FROM `${MangaColumn.tableName}` " +
            "WHERE `${MangaColumn.name}` IS :name")
    suspend fun itemOrNull(name: String): Manga?

    @Query("SELECT * FROM `${MangaColumn.tableName}` " +
            "WHERE `${MangaColumn.categories}` IS :category")
    suspend fun itemsWhereCategoryNotAll(category: String): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` " +
            "WHERE `${MangaColumn.name}` IS :name")
    fun loadItem(name: String): Flow<Manga?>

    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    fun loadItems(): Flow<List<Manga>>

    @Query("SELECT * FROM ${Shikimori.LibManga.viewName}")
    fun loadLibraryItems(): Flow<List<Shikimori.LibManga>>
}

suspend fun MangaDao.getFromPath(file: File): Manga? {
    return getItems().firstOrNull { getFullPath(it.path) == file }
}

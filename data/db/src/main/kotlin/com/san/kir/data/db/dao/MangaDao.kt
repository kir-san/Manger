package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.extend.SimplifiedManga
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface MangaDao : BaseDao<Manga> {
    @Query("SELECT * FROM `${Manga.tableName}`")
    suspend fun getItems(): List<Manga>

    @Query("SELECT * FROM ${SimplifiedManga.viewName}")
    suspend fun simpleItems(): List<SimplifiedManga>

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.name}` IS :name")
    suspend fun item(name: String): Manga

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.name}` IS :name")
    suspend fun itemOrNull(name: String): Manga?

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.category}` IS :category")
    suspend fun itemsWhereCategoryNotAll(category: String): List<Manga>

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.name}` IS :name")
    fun itemWhereName(name: String): Flow<Manga?>

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.id}` IS :id")
    fun itemWhereId(id: Long): Flow<Manga?>

    @Query("SELECT * FROM `${Manga.tableName}`")
    fun loadItems(): Flow<List<Manga>>
}

suspend fun MangaDao.getFromPath(file: File): Manga? {
    return getItems().firstOrNull { getFullPath(it.path) == file }
}

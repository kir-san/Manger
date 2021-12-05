package com.san.kir.data.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.Manga
import com.san.kir.data.models.SimpleManga
import com.san.kir.data.models.columns.MangaColumn
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface MangaDao : BaseDao<Manga> {
    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    suspend fun getItems(): List<Manga>

    @Query("SELECT ${MangaColumn.id}, ${MangaColumn.name}, " +
            "${MangaColumn.logo}, ${MangaColumn.color}, ${MangaColumn.populate}, " +
            "${MangaColumn.categories} " +
            "FROM `${MangaColumn.tableName}`")
    suspend fun simpleItems(): List<SimpleManga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.name}` IS :name")
    suspend fun item(name: String): Manga

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.name}` IS :name")
    suspend fun itemOrNull(name: String): Manga?

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category")
    suspend fun itemsWhereCategoryNotAll(category: String): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.name}` IS :name")
    fun loadItem(name: String): Flow<Manga?>

    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    fun loadItems(): Flow<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.id}` DESC")
    fun loadMangaAddTimeDesc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.name}` ASC")
    fun loadMangaAbcSortAsc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.name}` DESC")
    fun loadMangaAbcSortDesc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.populate}` DESC")
    fun loadMangaPopulateAsc(): DataSource.Factory<Int, Manga>
}

suspend fun MangaDao.getFromPath(file: File): Manga? {
    return getItems().firstOrNull { getFullPath(it.path) == file }
}

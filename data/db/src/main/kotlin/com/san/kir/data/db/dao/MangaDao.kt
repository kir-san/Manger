package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.extend.MiniManga
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

    @Query(
        "SELECT * FROM ${Manga.tableName} " +
                "WHERE ${Manga.Col.id} IS :id"
    )
    suspend fun itemById(id: Long): Manga

    @Query(
        "SELECT * FROM `${Manga.tableName}` " +
                "WHERE `${Manga.Col.name}` IS :name"
    )
    suspend fun itemOrNull(name: String): Manga?

    @Query(
        "SELECT * FROM ${Manga.tableName} " +
                "WHERE ${Manga.Col.categoryId} IS :id"
    )
    suspend fun itemsByCategoryId(id: Long): List<Manga>

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.name}` IS :name")
    fun itemWhereName(name: String): Flow<Manga?>

    @Query("SELECT * FROM `${Manga.tableName}` " +
            "WHERE `${Manga.Col.id}` IS :id")
    fun itemWhereId(id: Long): Flow<Manga?>

    @Query("SELECT * FROM `${Manga.tableName}`")
    fun loadItems(): Flow<List<Manga>>

    @Query("SELECT * FROM ${MiniManga.viewName}")
    fun loadMiniItems(): Flow<List<MiniManga>>

    @Query(
        "UPDATE ${Manga.tableName} " +
                "SET ${Manga.Col.update} = :isUpdate " +
                "WHERE ${Manga.Col.id} = :id"
    )
    fun update(id: Long, isUpdate: Boolean)
}

suspend fun MangaDao.getFromPath(file: File): Manga? {
    return getItems().firstOrNull { getFullPath(it.path) == file }
}

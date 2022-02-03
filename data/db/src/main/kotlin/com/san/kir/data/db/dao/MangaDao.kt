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

    // Получение flow со списком всех элементов
    @Query("SELECT * FROM ${Manga.tableName}")
    fun loadItems(): Flow<List<Manga>>

    // Получение flow со списком всех элементов из view
    @Query("SELECT * FROM ${MiniManga.viewName}")
    fun loadMiniItems(): Flow<List<MiniManga>>

    // Получение flow с элементом по его названию
    @Query(
        "SELECT * FROM `${Manga.tableName}` " +
                "WHERE `${Manga.Col.name}` IS :name"
    )
    fun loadItemByName(name: String): Flow<Manga?>

    // Получение flow с элементом по его id
    @Query(
        "SELECT * FROM ${Manga.tableName} " +
                "WHERE ${Manga.Col.id} IS :id"
    )
    fun loadItemById(id: Long): Flow<Manga?>

    // Получение всех элементов
    @Query("SELECT * FROM ${Manga.tableName}")
    suspend fun items(): List<Manga>

    // Получение всех упрощенных элементов из View
    @Query("SELECT * FROM ${SimplifiedManga.viewName}")
    suspend fun simpleItems(): List<SimplifiedManga>

    // Получение элемента по названию
    @Query(
        "SELECT * FROM ${Manga.tableName} " +
                "WHERE ${Manga.Col.name} IS :name"
    )
    suspend fun itemByName(name: String): Manga

    // Получение элемента по id
    @Query(
        "SELECT * FROM ${Manga.tableName} " +
                "WHERE ${Manga.Col.id} IS :id"
    )
    suspend fun itemById(id: Long): Manga

    // Получение элементов по id категории
    @Query(
        "SELECT * FROM ${Manga.tableName} " +
                "WHERE ${Manga.Col.categoryId} IS :id"
    )
    suspend fun itemsByCategoryId(id: Long): List<Manga>

    // Обновление поля isUpdate
    @Query(
        "UPDATE ${Manga.tableName} " +
                "SET ${Manga.Col.update} = :isUpdate " +
                "WHERE ${Manga.Col.id} = :id"
    )
    fun update(id: Long, isUpdate: Boolean)
}

// Получение элемента по его пути хранения
suspend fun MangaDao.itemByPath(file: File): Manga? {
    return items().firstOrNull { getFullPath(it.path) == file }
}

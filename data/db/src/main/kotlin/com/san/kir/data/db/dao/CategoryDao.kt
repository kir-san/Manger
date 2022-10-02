package com.san.kir.data.db.dao

import android.content.Context
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.data.models.base.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<Category> {

    @Query("SELECT COUNT(id) FROM categories")
    fun loadItemsCount(): Flow<Int>

    // Получение flow со списком всех элементов отсортированных по полю order
    @Query("SELECT * FROM categories ORDER BY ordering")
    fun loadItems(): Flow<List<Category>>

    // Получение flow со списком имен всех элементов отсортированных по полю order
    @Query("SELECT name FROM categories ORDER BY ordering")
    fun loadNames(): Flow<List<String>>

    // Получение flow с элементом по его названию
    @Query("SELECT * FROM categories WHERE name IS :name")
    fun loadItemByName(name: String): Flow<Category>

    // Получение flow с элементом по его id
    @Query("SELECT * FROM categories WHERE id IS :id")
    fun loadItemById(id: Long): Flow<Category>

    // Получение всех элементов отсортированных по полю Порядок
    @Query("SELECT * FROM categories ORDER BY ordering")
    suspend fun items(): List<Category>

    // Получение элемента по его id
    @Query("SELECT * FROM categories WHERE id IS :id")
    suspend fun itemById(id: Long): Category

    // Получение элемента по его названию
    @Query("SELECT * FROM categories WHERE name IS :name")
    suspend fun itemByName(name: String): Category

    // Получение категории по умолчанию
    suspend fun defaultCategory(ctx: Context) = itemByName(ctx.CATEGORY_ALL)
}



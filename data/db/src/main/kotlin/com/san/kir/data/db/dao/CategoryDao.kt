package com.san.kir.data.db.dao

import android.content.Context
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.extend.CategoryWithMangas
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<Category> {

    // Получение flow со списком всех элементов отсортированных по полю order
    @Query("SELECT * FROM ${Category.tableName} ORDER BY ${Category.Col.order}")
    fun loadItems(): Flow<List<Category>>

    // Получение flow со списком специальных элементов
    @Transaction
    @Query(
        "SELECT " +
                "${Category.Col.id}, " +
                "${Category.Col.name}, " +
                "${Category.Col.typeSort}, " +
                "${Category.Col.isReverseSort}, " +
                "${Category.Col.spanPortrait}, " +
                "${Category.Col.spanLandscape}, " +
                "${Category.Col.isLargePortrait}, " +
                "${Category.Col.isLargeLandscape} " +
                "FROM ${Category.tableName} " +
                "WHERE ${Category.Col.isVisible} IS 1 " +
                "ORDER BY ${Category.Col.order}"
    )
    fun loadSpecItems(): Flow<List<CategoryWithMangas>>

    // Получение flow с элементом по его названию
    @Query("SELECT * FROM ${Category.tableName} WHERE ${Category.Col.name} IS :name")
    fun loadItemByName(name: String): Flow<Category>

    // Получение flow с элементом по его id
    @Query("SELECT * FROM ${Category.tableName} WHERE ${Category.Col.id} IS :id")
    fun loadItemById(id: Long): Flow<Category>

    // Получение всех элементов отсортированных по полю Порядок
    @Query("SELECT * FROM ${Category.tableName} ORDER BY ${Category.Col.order}")
    suspend fun items(): List<Category>

    // Получение элемента по его id
    @Query("SELECT * FROM ${Category.tableName} WHERE ${Category.Col.id} IS :id")
    suspend fun itemById(id: Long): Category

    // Получение элемента по его названию
    @Query("SELECT * FROM ${Category.tableName} WHERE ${Category.Col.name} IS :name")
    suspend fun itemByName(name: String): Category
}

// Получение категории по умолчанию
suspend fun CategoryDao.defaultCategory(ctx: Context) = itemByName(ctx.CATEGORY_ALL)

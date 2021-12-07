package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.san.kir.data.models.Category
import com.san.kir.data.models.CategoryWithMangas
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao : BaseDao<Category> {
    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract suspend fun getItems(): List<Category>

    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun loadItems(): Flow<List<Category>>

    @Query("SELECT * FROM `categories` WHERE `name` IS :name")
    abstract fun loadItem(name: String): Flow<Category>

    @Transaction
    @Query("SELECT * FROM `categories` " +
            "WHERE isVisible IS 1 " +
            "ORDER BY `order`")
    abstract fun loadItemsAdds(): Flow<List<CategoryWithMangas>>
}
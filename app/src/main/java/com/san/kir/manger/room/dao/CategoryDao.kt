package com.san.kir.manger.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao:
    BaseDao<Category> {
    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract suspend fun getItems(): List<Category>

    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun loadItems(): Flow<List<Category>>

    @Query("SELECT * FROM `categories` WHERE `name` IS :name")
    abstract fun loadItem(name: String): Flow<Category>
}

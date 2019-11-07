package com.san.kir.manger.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.Category

@Dao
abstract class CategoryDao:
    BaseDao<Category> {
    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun getItems(): List<Category>

    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun loadItems(): LiveData<List<Category>>

    @Query("SELECT * FROM `categories` WHERE `name` IS :name")
    abstract fun loadItem(name: String): LiveData<Category>
}

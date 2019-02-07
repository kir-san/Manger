package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Category

@Dao
abstract class CategoryDao: BaseDao<Category> {
    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun getItems(): List<Category>

    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun loadItems(): LiveData<List<Category>>

    @Query("SELECT * FROM `categories` WHERE `name` IS :name")
    abstract fun loadItem(name: String): LiveData<Category>
}

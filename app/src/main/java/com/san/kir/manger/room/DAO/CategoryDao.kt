package com.san.kir.manger.room.DAO

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Category

@Dao
abstract class CategoryDao: BaseDao<Category> {
    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun loadCategories(): List<Category>

    @Query("SELECT * FROM `categories` ORDER BY `order`")
    abstract fun loadLiveCategories(): LiveData<List<Category>>
}

fun toStringList(categories: List<Category>) = categories.map { it.name }

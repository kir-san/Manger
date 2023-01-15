package com.san.kir.data.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

interface BaseDao<in T> {
    @Insert
    suspend fun insert(vararg item: T): List<Long>

    @Insert
    suspend fun insert(items: List<T>): List<Long>

    @Update
    suspend fun update(vararg item: T?): Int

    @Update
    suspend fun update(items: List<T?>): Int

    @Delete
    suspend fun delete(vararg item: T?): Int

    @Delete
    suspend fun delete(items: List<T?>): Int
}

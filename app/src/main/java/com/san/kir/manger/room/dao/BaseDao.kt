package com.san.kir.manger.room.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

interface BaseDao<in T> {
    @Insert
    suspend fun insert(vararg item: T)

    @Update
    suspend fun update(vararg item: T?): Int

    @Delete
    suspend fun delete(vararg item: T?): Int
}

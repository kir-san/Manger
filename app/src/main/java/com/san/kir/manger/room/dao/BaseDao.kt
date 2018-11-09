package com.san.kir.manger.room.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update

interface BaseDao<in T> {
    @Insert
    fun insert(vararg item: T)

    @Update
    fun update(vararg item: T?)

    @Delete
    fun delete(vararg item: T?)

}

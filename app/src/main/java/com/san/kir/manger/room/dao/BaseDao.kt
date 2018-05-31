package com.san.kir.manger.room.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update
import kotlinx.coroutines.experimental.async

interface BaseDao<in T> {
    @Insert
    fun insert(vararg item: T)

    @Update
    fun update(vararg item: T?)

    @Delete
    fun delete(vararg item: T?)

}

fun <T> BaseDao<T>.insertAsync(vararg items: T) = async {
    insert(*items)
}

fun <T> BaseDao<T>.updateAsync(vararg items: T?) = async {
    update(*items)
}

fun <T> BaseDao<T>.deleteAsync(vararg items: T?) = async {
    delete(*items)
}


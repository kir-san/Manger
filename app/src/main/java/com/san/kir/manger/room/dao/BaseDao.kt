package com.san.kir.manger.room.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface BaseDao<in T> {
    @Insert
    fun insert(vararg item: T)

    @Update
    fun update(vararg item: T?)

    @Delete
    fun delete(vararg item: T?)

}

fun <T> BaseDao<T>.insertAsync(vararg items: T) = GlobalScope.launch(Dispatchers.Main) {
    insert(*items)
}

fun <T> BaseDao<T>.updateAsync(vararg items: T?) = GlobalScope.launch(Dispatchers.Main) {
    update(*items)
}

fun <T> BaseDao<T>.deleteAsync(vararg items: T?) = GlobalScope.launch(Dispatchers.Main) {
    delete(*items)
}


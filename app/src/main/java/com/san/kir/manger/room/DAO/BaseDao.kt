package com.san.kir.manger.room.DAO

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update
import kotlinx.coroutines.experimental.async

interface BaseDao<in T> {
    @Insert
    fun add(vararg item: T)

    @Update
    fun upd(vararg item: T?)

    @Delete
    fun dlt(vararg item: T?)

}

fun <T> BaseDao<T>.insert(vararg items: T) = async {
    add(*items)
}

fun <T> BaseDao<T>.update(vararg items: T?) = async {
    upd(*items)
}

fun <T> BaseDao<T>.delete(vararg items: T?) = async {
    dlt(*items)
}


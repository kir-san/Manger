package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Storage

@Dao
interface StorageDao : BaseDao<Storage> {
    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun pagedItems(): DataSource.Factory<Int, Storage>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun loadItems(): LiveData<List<Storage>>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun loadItem(shortPath: String): LiveData<Storage?>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun getItems(): List<Storage>
}

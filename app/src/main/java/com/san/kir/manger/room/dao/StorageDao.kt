package com.san.kir.manger.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.Storage
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao : BaseDao<Storage> {
    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun flowItems(): Flow<List<Storage>>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun loadItems(): LiveData<List<Storage>>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun flowItem(shortPath: String): Flow<Storage?>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun loadItem(shortPath: String): LiveData<Storage?>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    suspend fun items(): List<Storage>
}

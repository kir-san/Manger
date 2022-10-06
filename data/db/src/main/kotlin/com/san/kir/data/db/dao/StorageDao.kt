package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.Storage
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao : BaseDao<Storage> {

    @Query("SELECT SUM(sizeFull) FROM StorageItem")
    fun loadFullSize(): Flow<Double>

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    fun loadItems(): Flow<List<Storage>>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    fun loadItemByPath(shortPath: String): Flow<Storage?>

    @Query("SELECT * FROM StorageItem WHERE path IS :shortPath")
    suspend fun itemByPath(shortPath: String): Storage?

    @Query("SELECT * FROM StorageItem ORDER BY sizeFull DESC")
    suspend fun items(): List<Storage>
}

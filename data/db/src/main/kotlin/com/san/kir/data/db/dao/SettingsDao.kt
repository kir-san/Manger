package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.san.kir.data.models.base.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings")
    fun loadItems(): Flow<Settings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: Settings)
}

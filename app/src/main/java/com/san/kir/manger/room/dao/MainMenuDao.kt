package com.san.kir.manger.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.MainMenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MainMenuDao : BaseDao<MainMenuItem> {
    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    suspend fun getItems(): List<MainMenuItem>

    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    fun loadItems(): Flow<List<MainMenuItem>>
}



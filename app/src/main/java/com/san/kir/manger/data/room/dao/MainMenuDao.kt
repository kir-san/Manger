package com.san.kir.manger.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.data.room.entities.MainMenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MainMenuDao : BaseDao<MainMenuItem> {
    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    suspend fun getItems(): List<MainMenuItem>

    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    fun loadItems(): Flow<List<MainMenuItem>>
}



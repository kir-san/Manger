package com.san.kir.manger.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.MainMenuItem

@Dao
interface MainMenuDao :
    BaseDao<MainMenuItem> {
    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    fun getItems(): List<MainMenuItem>
}



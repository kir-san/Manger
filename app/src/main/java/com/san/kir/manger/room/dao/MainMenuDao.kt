package com.san.kir.manger.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.MainMenuItem

@Dao
interface MainMenuDao : BaseDao<MainMenuItem> {
    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    fun getItems(): List<MainMenuItem>
}



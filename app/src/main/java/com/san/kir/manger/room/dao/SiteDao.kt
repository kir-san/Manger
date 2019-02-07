package com.san.kir.manger.room.dao

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Site

@Dao
interface SiteDao : BaseDao<Site> {
    @Query("SELECT * FROM sites")
    fun pagedItems(): DataSource.Factory<Int, Site>

    @Query("SELECT * FROM sites")
    fun getItems(): List<Site>

    @Query("SELECT * FROM sites WHERE name is :name")
    fun getItem(name: String): Site?
}

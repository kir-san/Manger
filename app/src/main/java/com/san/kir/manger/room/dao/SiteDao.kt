package com.san.kir.manger.room.dao

import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Site

@Dao
interface SiteDao : BaseDao<Site> {
    @Query("SELECT * FROM sites")
    fun loadSites(): DataSource.Factory<Int, Site>


    @Query("SELECT * FROM sites WHERE name is :name")
    fun loadSite(name: String): Site?
}

fun SiteDao.loadPagedSites() =
        LivePagedListBuilder(loadSites(), 20).build()

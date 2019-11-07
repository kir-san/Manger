package com.san.kir.manger.repositories

import android.content.Context
import androidx.paging.LivePagedListBuilder
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.room.getDatabase

class SiteRepository(context: Context) {
    private val db = getDatabase(context)
    private val mSiteDao = db.siteDao

    fun pagedItems() = mSiteDao.pagedItems()
    suspend fun getItems() = mSiteDao.getItems()
    fun getItem(name: String) = mSiteDao.getItem(name)
    suspend fun update(vararg site: Site?) = mSiteDao.update(*site)
    suspend fun insert(vararg site: Site) = mSiteDao.insert(*site)
    suspend fun delete(vararg site: Site) = mSiteDao.delete(*site)
    fun loadPagedItems() = LivePagedListBuilder(pagedItems(), 20).build()
}


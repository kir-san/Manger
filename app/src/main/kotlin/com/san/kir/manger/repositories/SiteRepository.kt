package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.data.db.RoomDB
import com.san.kir.data.models.Site

class SiteRepository(context: Context) {
    private val db = RoomDB.getDatabase(context)
    private val mSiteDao = db.siteDao

    suspend fun getItems() = mSiteDao.getItems()
    fun getItem(name: String) = mSiteDao.getItem(name)
    suspend fun update(vararg site: Site?) = mSiteDao.update(*site)
    suspend fun insert(vararg site: Site) = mSiteDao.insert(*site)
    suspend fun delete(vararg site: Site) = mSiteDao.delete(*site)

    fun loadItems() = mSiteDao.loadItems()
}


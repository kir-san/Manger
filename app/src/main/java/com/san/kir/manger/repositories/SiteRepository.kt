package com.san.kir.manger.repositories

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.Site
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SiteRepository(context: Context) {
    private val db = getDatabase(context)
    private val mSiteDao = db.siteDao

    fun pagedItems(): DataSource.Factory<Int, Site> {
        return mSiteDao.pagedItems()
    }

    fun getItems(): List<Site> {
        return mSiteDao.getItems()
    }

    fun getItem(name: String): Site? {
        return mSiteDao.getItem(name)
    }

    fun update(vararg site: Site?) = GlobalScope.launch { mSiteDao.update(*site) }
    fun insert(vararg site: Site) = GlobalScope.launch { mSiteDao.insert(*site) }
    fun delete(vararg site: Site) = GlobalScope.launch { mSiteDao.delete(*site) }

    fun loadPagedItems(): LiveData<PagedList<Site>> {
        return LivePagedListBuilder(pagedItems(), 20).build()
    }
}


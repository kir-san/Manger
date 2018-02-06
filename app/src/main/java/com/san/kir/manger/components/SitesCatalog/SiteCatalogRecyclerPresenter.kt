package com.san.kir.manger.components.SitesCatalog

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Parsing.SiteCatalog
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.DAO.loadPagedSites
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Site
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory

class SiteCatalogRecyclerPresenter(private val act: SiteCatalogActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
            .createPaging({ SiteCatalogItemView() },
                          { oldItem, newItem -> oldItem.id == newItem.id },
                          { oldItem, newItem -> oldItem == newItem })
    private val dao = Main.db.siteDao

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = adapter
        dao.loadPagedSites().observe(act, Observer(adapter::setList))
    }

    fun update() {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            it.volume = 0
            save(it)
        }
    }

    fun updateSitesInfo() {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    private fun save(site: SiteCatalog) {
        val s = dao.loadSite(site.name)
        if (s != null) {
            s.volume = site.volume
            s.host = site.host
            s.catalogName = site.catalogName
            dao.update(s)
        } else {
            dao.insert(Site(name = site.name,
                            host = site.host,
                            catalogName = site.catalogName,
                            volume = site.volume,
                            oldVolume = site.oldVolume,
                            siteID = site.ID))
        }
    }
}

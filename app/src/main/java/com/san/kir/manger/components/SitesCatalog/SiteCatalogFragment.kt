package com.san.kir.manger.components.SitesCatalog

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.utils.showAlways
import org.jetbrains.anko.include

class SiteCatalogFragment : Fragment() {
    private lateinit var siteCatalog: SiteCatalogAdapter

    private val recycler by lazy {
        context.include<RecyclerView>(R.layout.recycler_view) {
            layoutManager = LinearLayoutManager(context)
        }
    }

    /* перезаписанные функции */

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // установить меню из фрагмента
        setHasOptionsMenu(true)

        return recycler
    }

    override fun onResume() {
        super.onResume()
        siteCatalog = SiteCatalogAdapter()
        recycler.adapter = siteCatalog
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        menu.clear() // очистить меню перед созданием нового
        menu.add(0, 0, 0, R.string.catalog_for_one_site_update_all)
                .showAlways().setIcon(R.drawable.ic_update)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateSites() // выполнить задачу по обновлению каталога сайтов
        }
        return true
    }

    /* приватные функции */

    private fun updateSites() {


        siteCatalog.clear() // Очистить каталог

        ManageSites.CATALOG_SITES.forEach { it.isInit = false }

        siteCatalog.addAll(ManageSites.CATALOG_SITES)
    }
}

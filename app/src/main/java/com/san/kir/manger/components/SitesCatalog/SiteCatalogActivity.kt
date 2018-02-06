package com.san.kir.manger.components.SitesCatalog

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.Extending.Views.showAlways
import com.san.kir.manger.Extending.Views.showNever
import com.san.kir.manger.R
import com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteUpdaterService
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.components.Parsing.ManageSites
import org.jetbrains.anko.include
import org.jetbrains.anko.startService

class SiteCatalogActivity : DrawerActivity() {
    private val adapter = SiteCatalogRecyclerPresenter(this)

    override val LinearLayout.customView: View
        get() = include<RecyclerView>(R.layout.recycler_view) {
            layoutManager = LinearLayoutManager(context)
            this@SiteCatalogActivity.adapter.into(this)
        }

    override fun onResume() {
        super.onResume()
        setTitle(R.string.main_menu_catalogs)
        adapter.updateSitesInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.catalog_for_one_site_update_all)
                .showAlways()
                .setIcon(R.drawable.ic_update)
        menu.add(0, 1, 0, "Обновить содержимое каталогов")
                .showNever()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> adapter.update()
            1 -> updateCatalogs()
        }
        return true
    }

    private fun updateCatalogs() {
        ManageSites.CATALOG_SITES.forEach {
            if (!CatalogForOneSiteUpdaterService.isContain(it.ID))
                startService<CatalogForOneSiteUpdaterService>("id" to it.ID)
        }
    }
}

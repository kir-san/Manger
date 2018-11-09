package com.san.kir.manger.components.sitesCatalog

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.catalogForOneSite.CatalogForOneSiteUpdaterService
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.extending.views.showNever
import kotlinx.coroutines.asCoroutineDispatcher
import org.jetbrains.anko.include
import org.jetbrains.anko.startService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SiteCatalogActivity : DrawerActivity() {
    val dispatcher: CoroutineContext by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher() + job
    }
    private val adapter by lazy {
        SiteCatalogRecyclerPresenter(this)
    }

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
        menu.add(0, 1, 0, R.string.catalog_for_one_site_update_catalog_contain)
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
            if (!CatalogForOneSiteUpdaterService.isContain(it.id))
                startService<CatalogForOneSiteUpdaterService>("id" to it.id)
        }
    }
}

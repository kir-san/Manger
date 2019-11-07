package com.san.kir.manger.components.sites_catalog

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.san.kir.ankofork.include
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.startService
import com.san.kir.manger.R
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteUpdaterService
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.showNever
import com.san.kir.manger.view_models.SiteCatalogViewModel

class SiteCatalogActivity : DrawerActivity() {
    private val mAdapter by lazy { SiteCatalogRecyclerPresenter(this) }
    val mViewModel by viewModels<SiteCatalogViewModel>()

    override val _LinearLayout.customView: View
        get() = include<androidx.recyclerview.widget.RecyclerView>(R.layout.recycler_view) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            mAdapter.into(this)
        }

    override fun onResume() {
        super.onResume()
        setTitle(R.string.main_menu_catalogs)
        mViewModel.updateSitesInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.catalog_for_one_site_update_all)
        menu.add(0, 1, 0, R.string.catalog_for_one_site_update_catalog_contain)
            .showNever()
        menu.add(0, 2, 2, R.string.catalog_for_one_site_global_search)
            .showAlways()
            .setIcon(R.drawable.ic_action_search)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> mViewModel.update()
            1 -> updateCatalogs()
            2 -> startActivity<GlobalSearchActivity>()
        }
        return true
    }

    private fun updateCatalogs() {
        ManageSites.CATALOG_SITES.forEach {
            if (!CatalogForOneSiteUpdaterService.isContain(it.catalogName))
                startService<CatalogForOneSiteUpdaterService>("catalogName" to it.catalogName)
        }
    }
}

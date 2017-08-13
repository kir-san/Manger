package com.san.kir.manger.components.SitesCatalog

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Parsing.SiteCatalog
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class SiteCatalogAdapter :
        RecyclerView.Adapter<SiteCatalogViewHolder>() {
    private val catalog: MutableList<SiteCatalog> = mutableListOf()

    init {
        addAll(ManageSites.CATALOG_SITES)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteCatalogViewHolder? {
        return SiteCatalogViewHolder(SiteCatalogItemView(), parent)
    }

    override fun onBindViewHolder(holder: SiteCatalogViewHolder, position: Int) {
        holder.bind(catalog[position])
    }

    override fun getItemCount() = catalog.size

    fun add(site: SiteCatalog) {
        catalog.add(site)
        launch(UI) {
            notifyItemInserted(catalog.size - 1)
        }
    }

    fun addAll(sites: List<SiteCatalog>) {
        catalog.addAll(sites)
        launch(UI) {
            notifyDataSetChanged()
        }
    }

    fun clear() {
        catalog.clear()
        launch(UI) {
            notifyDataSetChanged()
        }
    }

}

package com.san.kir.manger.components.SitesCatalog

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.components.Parsing.SiteCatalog
import kotlinx.coroutines.experimental.newSingleThreadContext

class SiteCatalogViewHolder(val viewCatalog: SiteCatalogItemView, parent: ViewGroup) :
        RecyclerView.ViewHolder(viewCatalog.createView(parent)) {

    private val loadContext = newSingleThreadContext("volumeContext")

    fun bind(el: SiteCatalog) {
        viewCatalog.bind(el, loadContext)
    }

}

package com.san.kir.manger.components.sites_catalog

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory

class SiteCatalogRecyclerPresenter(private val act: SiteCatalogActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createPaging({ SiteCatalogItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = adapter
        act.mViewModel.getSiteItems().observe(act, Observer(adapter::submitList))
    }
}

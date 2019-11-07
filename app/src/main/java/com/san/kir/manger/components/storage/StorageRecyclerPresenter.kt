package com.san.kir.manger.components.storage

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StorageRecyclerPresenter(private val act: StorageActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory
        .createSimple { StorageItemView(act) }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = this.adapter
        act.lifecycleScope.launch {
            act.mViewModel
                .flowItems()
                .collect {
                    if (adapter.items != it) {
                        log("collect")
                        adapter.items = it
                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }
}

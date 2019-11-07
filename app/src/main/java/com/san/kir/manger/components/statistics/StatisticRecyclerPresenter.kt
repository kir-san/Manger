package com.san.kir.manger.components.statistics

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory

class StatisticRecyclerPresenter(private val act: StatisticActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory
        .createPaging({ StatisticItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = this.adapter
        act.mViewModel.getStatisticPagedItems()
            .observe(act, Observer(adapter::submitList))
    }
}

package com.san.kir.manger.components.statistics

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StatisticRecyclerPresenter(private val act: StatisticActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory
        .createPaging({ StatisticItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = this.adapter
        act.mViewModel.getStatisticPagedItems()
            .observe(act, Observer { act.launch(Dispatchers.Main) { adapter.submitList(it) } })
    }
}

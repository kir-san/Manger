package com.san.kir.manger.components.statistics

import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StatisticRecyclerPresenter(private val act: StatisticActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory
        .createPaging({ StatisticItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = this.adapter
        act.mViewModel.viewModelScope.launch {
            act.mViewModel.getStatisticPagedItems()
                .collect { adapter.submitList(it) }
        }
    }
}

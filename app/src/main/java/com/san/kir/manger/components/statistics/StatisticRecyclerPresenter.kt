package com.san.kir.manger.components.statistics

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.loadPagedItems
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatisticRecyclerPresenter(private val act: StatisticActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory
        .createPaging({ StatisticItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = this.adapter
        Main.db.statisticDao
            .loadPagedItems()
            .observe(act, Observer { GlobalScope.launch(Dispatchers.Main) { adapter.submitList(it) } })
    }
}

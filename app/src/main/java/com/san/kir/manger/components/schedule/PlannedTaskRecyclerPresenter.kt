package com.san.kir.manger.components.schedule

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory

class PlannedTaskRecyclerPresenter(private val act: ScheduleActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createPaging({ PlannedTaskItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        act.mViewModel.getPlannedItems()
            .observe(act, Observer(adapter::submitList))
    }
}

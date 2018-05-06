package com.san.kir.manger.components.schedule

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.loadPagedPlannedTasks
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

class PlannedTaskRecyclerPresenter(private val act: ScheduleActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createPaging({ PlannedTaskItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        Main.db.plannedDao.loadPagedPlannedTasks()
            .observe(act, Observer { it ->
                async(UI) {
                    adapter.setList(it)
                }
            })
    }
}

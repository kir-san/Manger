package com.san.kir.manger.components.schedule

import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.matchParent
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.ankofork.recyclerview.recyclerView


class PlannedTaskView(private val act: ScheduleActivity) : ActivityView() {
    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        recyclerView {
            lparams(width = matchParent, height = matchParent)
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(act)
            PlannedTaskRecyclerPresenter(act).into(this)
        }
    }
}

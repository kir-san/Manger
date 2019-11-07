package com.san.kir.manger.components.schedule

import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.ankofork.recyclerview.recyclerView

class AvailableUpdateView(private val act: ScheduleActivity) : ActivityView() {
    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = matchParent)

            recyclerView {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                setHasFixedSize(true)
                AvailableUpdateRecyclerPresenter(act).into(this)
            }.lparams(width = matchParent, height = matchParent)
        }
    }
}

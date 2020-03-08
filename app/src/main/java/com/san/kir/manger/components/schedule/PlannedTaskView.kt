package com.san.kir.manger.components.schedule

import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets


class PlannedTaskView(private val act: ScheduleActivity) : ActivityView() {
    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        recyclerView {
            lparams(width = matchParent, height = matchParent)
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(act)
            PlannedTaskRecyclerPresenter(act).into(this)

            clipToPadding = false

            doOnApplyWindowInstets { view, insets, padding ->
                view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                insets
            }
        }
    }
}

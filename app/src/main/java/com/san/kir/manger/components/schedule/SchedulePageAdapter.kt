package com.san.kir.manger.components.schedule

import com.san.kir.manger.R
import com.san.kir.manger.components.library.Page
import com.san.kir.manger.utils.PreparePagerAdapter

class SchedulePageAdapter(act: ScheduleActivity) : PreparePagerAdapter() {
    init {
        pagers = pagers + Page(
            act.getString(R.string.planned_task_name),
            PlannedTaskView(act).createView(act)
        )
        pagers = pagers + Page(
            act.getString(R.string.available_update_name),
            AvailableUpdateView(act).createView(act)
        )

        notifyDataSetChanged()
    }
}

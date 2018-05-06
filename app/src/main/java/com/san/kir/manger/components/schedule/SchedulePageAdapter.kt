package com.san.kir.manger.components.schedule

import com.san.kir.manger.R
import com.san.kir.manger.components.library.Page
import com.san.kir.manger.utils.PreparePagerAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class SchedulePageAdapter(act: ScheduleActivity) : PreparePagerAdapter() {
    init {
        launch(UI) {
            pagers += Page(
                act.getString(R.string.planned_task_name),
                PlannedTaskView(act).createView(act)
            )
            pagers += Page(
                act.getString(R.string.available_update_name),
                AvailableUpdateView().createView(act)
            )

            notifyDataSetChanged()
        }
    }
}

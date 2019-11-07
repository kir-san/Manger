package com.san.kir.manger.components.schedule

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.lines
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.switch
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.PlannedAddEdit
import com.san.kir.manger.utils.enums.PlannedPeriod
import com.san.kir.manger.utils.enums.PlannedType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PlannedTaskItemView(private val act: ScheduleActivity) :
    RecyclerViewAdapterFactory.AnkoView<PlannedTask>() {

    private val alarmManager = ScheduleManager()

    private lateinit var ctx: Context
    private lateinit var root: LinearLayout
    private lateinit var name: TextView
    private lateinit var updateText: TextView
    private lateinit var switch: Switch

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = dip(60))
            gravity = Gravity.CENTER_VERTICAL

            verticalLayout {
                name = textView {
                    textSize = 16f
                    lines = 1
                }

                updateText = textView {
                    textSize = 14f
                }

            }.lparams(width = matchParent, height = wrapContent) {
                weight = 1f
                leftMargin = dip(16)
            }

            switch = switch {
            }.lparams {
                margin = dip(16)
            }

            root = this
        }
    }

    override fun bind(item: PlannedTask, isSelected: Boolean, position: Int) {
        ctx = root.context

        when (item.type) {
            PlannedType.MANGA -> {
                name.text = ctx.getString(R.string.planned_task_name_manga, item.manga)
            }
            PlannedType.CATEGORY -> {
                name.text = ctx.getString(R.string.planned_task_name_category, item.category)
            }
            PlannedType.GROUP -> {
                name.text = ctx.getString(R.string.planned_task_name_group, item.groupName)
            }
            PlannedType.CATALOG -> {
                name.text = ctx.getString(R.string.planned_task_name_catalog, item.catalog)
            }
            PlannedType.APP -> {
                name.text = ctx.getString(R.string.planned_task_name_app)
            }
        }

        val dayText: String =
            if (item.period == PlannedPeriod.DAY) {
                ctx.getString(R.string.planned_task_update_text_day)
            } else {
                when (item.dayOfWeek) {
                    Calendar.MONDAY -> ctx.getString(R.string.planned_task_update_text_monday)
                    Calendar.TUESDAY -> ctx.getString(R.string.planned_task_update_text_tuesday)
                    Calendar.WEDNESDAY -> ctx.getString(R.string.planned_task_update_text_wednesday)
                    Calendar.THURSDAY -> ctx.getString(R.string.planned_task_update_text_thursday)
                    Calendar.FRIDAY -> ctx.getString(R.string.planned_task_update_text_friday)
                    Calendar.SATURDAY -> ctx.getString(R.string.planned_task_update_text_saturday)
                    Calendar.SUNDAY -> ctx.getString(R.string.planned_task_update_text_sunday)
                    else -> ctx.getString(R.string.planned_task_update_text_unknown)
                }
            }
        updateText.text =
            ctx.getString(
                R.string.planned_task_update_text_template,
                dayText,
                item.hour,
                String.format("%02d", item.minute)
            )

        switch.isChecked = item.isEnabled
        switch.onClick {
            act.lifecycleScope.launch(Dispatchers.Main) {
                item.isEnabled = !item.isEnabled
                act.mViewModel.plannedUpdate(item)

                if (item.isEnabled) {
                    alarmManager.add(item)
                } else {
                    alarmManager.cancel(act, item)
                }
            }
        }

        root.onClick {
            ctx.startActivity<AddEditPlannedTaskActivity>(PlannedAddEdit.edit to item)
        }
    }
}

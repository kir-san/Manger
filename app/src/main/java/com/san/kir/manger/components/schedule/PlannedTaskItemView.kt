package com.san.kir.manger.components.schedule

import android.content.Context
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.room.models.PlannedAddEdit
import com.san.kir.manger.room.models.PlannedPeriod
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.PlannedType
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentStart
import org.jetbrains.anko.bottomOf
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.switch
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalMargin
import java.util.*

class PlannedTaskItemView(private val act: ScheduleActivity) :
    RecyclerViewAdapterFactory.AnkoView<PlannedTask>() {

    private val alarmManager = ScheduleManager()

    private lateinit var ctx: Context
    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var updateText: TextView
    private lateinit var switch: Switch

    private object Id {
        val name = ID.generate()
        val switch = ID.generate()
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams {
                margin = dip(3)
                verticalMargin = dip(6)
            }

            switch = switch {
                id = Id.switch
            }.lparams {
                alignParentEnd()
                centerVertically()
                margin = dip(3)
            }

            name = textView {
                id = Id.name
                textSize = 18f
                lines = 1
            }.lparams {
                margin = dip(3)
                leftOf(Id.switch)
            }

            updateText = textView().lparams {
                alignParentStart()
                bottomOf(Id.name)
                leftOf(Id.switch)
                margin = dip(3)
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
            withContext(act.coroutineContext) {
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

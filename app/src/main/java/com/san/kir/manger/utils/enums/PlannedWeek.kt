package com.san.kir.manger.utils.enums

import android.content.Context
import com.san.kir.manger.R
import java.util.*

object PlannedWeek {
    fun map(context: Context) = mapOf(
        context.getString(R.string.planned_week_monday) to Calendar.MONDAY,
        context.getString(R.string.planned_week_tuesday) to Calendar.TUESDAY,
        context.getString(R.string.planned_week_wednesday) to Calendar.WEDNESDAY,
        context.getString(R.string.planned_week_thursday) to Calendar.THURSDAY,
        context.getString(R.string.planned_week_friday) to Calendar.FRIDAY,
        context.getString(R.string.planned_week_saturday) to Calendar.SATURDAY,
        context.getString(R.string.planned_week_sunday) to Calendar.SUNDAY
    )
}

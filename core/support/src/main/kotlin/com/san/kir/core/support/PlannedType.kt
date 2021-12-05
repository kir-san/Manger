package com.san.kir.core.support

import com.san.kir.core.support.R
import java.util.*

enum class PlannedType(
    val order: Int,
    val text: Int
) {
    MANGA(1, R.string.planned_type_manga),
    GROUP(2, R.string.planned_type_group),
    CATEGORY(3, R.string.planned_type_category),
    CATALOG(4, R.string.planned_type_catalog),
    APP(5, R.string.planned_type_app)
}

enum class PlannedPeriod(
    val order: Int,
    val text: Int,
) {
    DAY(1, R.string.planned_period_day),
    WEEK(2, R.string.planned_period_week)
}

enum class PlannedWeek(
    val order: Int,
    val text: Int,
) {
    MONDAY(Calendar.MONDAY, R.string.planned_week_monday),
    TUESDAY(Calendar.TUESDAY, R.string.planned_week_tuesday),
    WEDNESDAY(Calendar.WEDNESDAY, R.string.planned_week_wednesday),
    THURSDAY(Calendar.THURSDAY, R.string.planned_week_thursday),
    FRIDAY(Calendar.FRIDAY, R.string.planned_week_friday),
    SATURDAY(Calendar.SATURDAY, R.string.planned_week_saturday),
    SUNDAY(Calendar.SUNDAY, R . string . planned_week_sunday),
}

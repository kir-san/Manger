package com.san.kir.core.support

import java.util.Calendar

enum class PlannedType(
    val order: Int,
    val text: Int,
) {
    MANGA(1, R.string.manga),
    GROUP(2, R.string.group),
    CATEGORY(3, R.string.category),
    CATALOG(4, R.string.catalog),
    APP(5, R.string.app)
}

enum class PlannedPeriod(
    val order: Int,
    val text: Int,
    val dayText: Int,
) {
    DAY(1, R.string.day, R.string.once_by_day),
    WEEK(2, R.string.week, -1)
}

enum class PlannedWeek(
    val order: Int,
    val text: Int,
    val dayText: Int,
) {
    MONDAY(Calendar.MONDAY, R.string.mon, R.string.every_monday),
    TUESDAY(Calendar.TUESDAY, R.string.tue, R.string.every_tuesday),
    WEDNESDAY(Calendar.WEDNESDAY, R.string.wed, R.string.every_wednesday),
    THURSDAY(Calendar.THURSDAY, R.string.thu, R.string.every_thursday),
    FRIDAY(Calendar.FRIDAY, R.string.fri, R.string.every_friday),
    SATURDAY(Calendar.SATURDAY, R.string.sat, R.string.every_saturday),
    SUNDAY(Calendar.SUNDAY, R.string.sun, R.string.every_sunday),
}

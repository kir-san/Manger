package com.san.kir.manger.utils.enums

import android.content.Context
import com.san.kir.manger.R

object PlannedPeriod {
    const val DAY = 1
    const val WEEK = 2

    fun map(context: Context) = mapOf(
        context.getString(R.string.planned_period_day) to DAY,
        context.getString(R.string.planned_period_week) to WEEK
    )
}

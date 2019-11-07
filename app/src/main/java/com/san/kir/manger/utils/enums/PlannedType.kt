package com.san.kir.manger.utils.enums

import android.content.Context
import com.san.kir.manger.R

object PlannedType {
    const val MANGA = 1
    const val GROUP = 2
    const val CATEGORY = 3
    const val CATALOG = 4
    const val APP = 5

    fun map(context: Context) = mapOf(
        context.getString(R.string.planned_type_manga) to MANGA,
        context.getString(R.string.planned_type_group) to GROUP,
        context.getString(R.string.planned_type_category) to CATEGORY,
        context.getString(R.string.planned_type_catalog) to CATALOG,
        context.getString(R.string.planned_type_app) to APP
    )
}

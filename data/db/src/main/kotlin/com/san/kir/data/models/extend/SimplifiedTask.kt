package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.data.models.base.PlannedTaskBase

data class SimplifiedTask(
    @ColumnInfo(name = "id") override val id: Long = 0L,
    @ColumnInfo(name = "manga") override val manga: String = "",
    @ColumnInfo(name = "group_name") override val groupName: String = "",
    @ColumnInfo(name = "category") override val category: String = "",
    @ColumnInfo(name = "catalog") override val catalog: String = "",
    @ColumnInfo(name = "type") override val type: PlannedType = PlannedType.MANGA,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = false,
    @ColumnInfo(name = "period") override val period: PlannedPeriod = PlannedPeriod.DAY,
    @ColumnInfo(name = "day_of_week") override val dayOfWeek: PlannedWeek = PlannedWeek.MONDAY,
    @ColumnInfo(name = "hour") override val hour: Int = 0,
    @ColumnInfo(name = "minute") override val minute: Int = 0,
) : PlannedTaskBase

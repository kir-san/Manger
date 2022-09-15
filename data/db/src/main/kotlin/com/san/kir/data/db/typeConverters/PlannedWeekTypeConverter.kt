package com.san.kir.data.db.typeConverters

import androidx.room.TypeConverter
import com.san.kir.core.support.PlannedWeek

internal class PlannedWeekTypeConverter {
    @TypeConverter
    fun typeToInt(type: PlannedWeek): Int = type.order

    @TypeConverter
    fun intToType(type: Int): PlannedWeek = PlannedWeek.values().first { it.order == type }
}

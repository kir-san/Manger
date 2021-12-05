package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter
import com.san.kir.core.support.PlannedWeek

class PlannedWeekTypeConverter {
    @TypeConverter
    fun typeToInt(type: PlannedWeek): Int = type.order

    @TypeConverter
    fun intToType(type: Int): PlannedWeek = PlannedWeek.values().first { it.order == type }
}

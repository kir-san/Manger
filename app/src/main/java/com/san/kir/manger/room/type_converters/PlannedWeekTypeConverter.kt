package com.san.kir.manger.room.type_converters

import androidx.room.TypeConverter
import com.san.kir.manger.utils.enums.PlannedWeek

class PlannedWeekTypeConverter {
    @TypeConverter
    fun typeToInt(type: PlannedWeek): Int = type.order

    @TypeConverter
    fun intToType(type: Int): PlannedWeek = PlannedWeek.values().first { it.order == type }
}

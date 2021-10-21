package com.san.kir.manger.room.type_converters

import androidx.room.TypeConverter
import com.san.kir.manger.utils.enums.PlannedPeriod

class PlannedPeriodTypeConverter {
    @TypeConverter
    fun typeToInt(type: PlannedPeriod): Int = type.order

    @TypeConverter
    fun intToType(type: Int): PlannedPeriod = PlannedPeriod.values().first { it.order == type }
}

package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter
import com.san.kir.core.support.PlannedPeriod

internal class PlannedPeriodTypeConverter {
    @TypeConverter
    fun typeToInt(type: PlannedPeriod): Int = type.order

    @TypeConverter
    fun intToType(type: Int): PlannedPeriod = PlannedPeriod.values().first { it.order == type }
}

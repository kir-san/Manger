package com.san.kir.manger.data.room.type_converters

import androidx.room.TypeConverter
import com.san.kir.manger.utils.enums.PlannedType

class PlannedTypeTypeConverter {
    @TypeConverter
    fun typeToInt(type: PlannedType): Int = type.order

    @TypeConverter
    fun intToType(type: Int): PlannedType = PlannedType.values().first { it.order == type }
}

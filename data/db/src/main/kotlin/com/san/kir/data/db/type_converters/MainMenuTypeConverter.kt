package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter
import com.san.kir.core.support.MainMenuType

class MainMenuTypeConverter {
    @TypeConverter
    fun typeToString(type: MainMenuType): String = type.name

    @TypeConverter
    fun stringToType(name: String): MainMenuType = MainMenuType.valueOf(name)
}

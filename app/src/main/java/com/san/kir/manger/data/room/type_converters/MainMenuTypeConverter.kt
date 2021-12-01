package com.san.kir.manger.data.room.type_converters

import androidx.room.TypeConverter
import com.san.kir.manger.utils.enums.MainMenuType

class MainMenuTypeConverter {
    @TypeConverter
    fun typeToString(type: MainMenuType): String = type.name

    @TypeConverter
    fun stringToType(name: String): MainMenuType = MainMenuType.valueOf(name)
}

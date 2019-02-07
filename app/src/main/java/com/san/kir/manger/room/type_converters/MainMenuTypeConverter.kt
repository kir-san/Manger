package com.san.kir.manger.room.type_converters

import android.arch.persistence.room.TypeConverter
import com.san.kir.manger.components.drawer.MainMenuType

class MainMenuTypeConverter {
    @TypeConverter
    fun typeToString(type: MainMenuType): String = type.name

    @TypeConverter
    fun stringToType(name: String): MainMenuType = MainMenuType.valueOf(name)
}

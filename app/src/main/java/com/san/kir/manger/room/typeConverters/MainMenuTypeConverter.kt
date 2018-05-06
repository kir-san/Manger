package com.san.kir.manger.room.typeConverters

import android.arch.persistence.room.TypeConverter
import com.san.kir.manger.room.dao.MainMenuType

class MainMenuTypeConverter {
    @TypeConverter
    fun typeToString(type: MainMenuType): String = type.name

    @TypeConverter
    fun stringToType(name: String): MainMenuType = MainMenuType.valueOf(name)
}

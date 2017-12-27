package com.san.kir.manger.room.TypeConverters

import android.arch.persistence.room.TypeConverter
import com.san.kir.manger.room.DAO.MainMenuType

class MainMenuTypeConverter {
    @TypeConverter
    fun typeToString(type: MainMenuType): String = type.name

    @TypeConverter
    fun stringToType(name: String): MainMenuType = MainMenuType.valueOf(name)
}

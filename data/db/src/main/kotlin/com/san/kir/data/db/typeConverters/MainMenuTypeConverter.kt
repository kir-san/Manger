package com.san.kir.data.db.typeConverters

import androidx.room.TypeConverter
import com.san.kir.core.support.MainMenuType

internal class MainMenuTypeConverter {
    @TypeConverter
    fun typeToString(type: MainMenuType): String = type.name

    @TypeConverter
    fun stringToType(name: String): MainMenuType = MainMenuType.valueOf(name)
}

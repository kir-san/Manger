package com.san.kir.manger.room.typeConverters

import android.arch.persistence.room.TypeConverter

class ListStringConverter {
    @TypeConverter
    fun listToString(list: List<String>): String = list.toString().removeSurrounding("[", "]")

    @TypeConverter
    fun stringToList(string: String): List<String> =
            string.split(",")
                    .map { it.removePrefix(" ").removeSuffix(" ") }
}

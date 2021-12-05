package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter

class ListStringConverter {
    @TypeConverter
    fun listToString(list: List<String>): String = list.toString().removeSurrounding("[", "]")

    @TypeConverter
    fun stringToList(string: String): List<String> =
            string.split(",")
                    .map { it.removePrefix(" ").removeSuffix(" ") }
}


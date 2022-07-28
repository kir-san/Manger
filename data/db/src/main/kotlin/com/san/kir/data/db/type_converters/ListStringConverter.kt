package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter

internal class ListStringConverter {
    @TypeConverter
    fun listToString(list: List<String>): String {
        return list.toString().removeSurrounding("[", "]")
    }

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return string.split(",").map { it.removeSurrounding(" ", " ") }
    }
}


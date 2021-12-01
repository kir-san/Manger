package com.san.kir.manger.data.room.type_converters

import androidx.room.TypeConverter
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.shortPath
import java.io.File


@Suppress("unused")
class FileConverter {
    @TypeConverter
    fun fileToString(file: File): String = file.shortPath

    @TypeConverter
    fun stringToFile(path: String): File = getFullPath(path)
}

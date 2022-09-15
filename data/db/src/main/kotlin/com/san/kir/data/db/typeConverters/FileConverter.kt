package com.san.kir.data.db.typeConverters

import androidx.room.TypeConverter
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import java.io.File

@Suppress("unused")
internal class FileConverter {
    @TypeConverter
    fun fileToString(file: File): String = file.shortPath

    @TypeConverter
    fun stringToFile(path: String): File = getFullPath(path)
}

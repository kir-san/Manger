package com.san.kir.manger.room.typeConverters

import android.arch.persistence.room.TypeConverter
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.shortPath
import java.io.File


class FileConverter {
    @TypeConverter
    fun fileToString(file: File): String = file.shortPath

    @TypeConverter
    fun stringToFile(path: String): File = getFullPath(path)
}

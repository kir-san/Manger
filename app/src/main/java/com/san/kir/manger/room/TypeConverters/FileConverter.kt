package com.san.kir.manger.room.TypeConverters

import android.arch.persistence.room.TypeConverter
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getShortPath
import java.io.File


class FileConverter {
    @TypeConverter
    fun fileToString(file: File): String = getShortPath(file)

    @TypeConverter
    fun stringToFile(path: String): File = getFullPath(path)
}

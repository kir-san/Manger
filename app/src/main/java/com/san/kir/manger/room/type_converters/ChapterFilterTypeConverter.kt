package com.san.kir.dblib.type_converters

import android.arch.persistence.room.TypeConverter
import com.san.kir.manger.utils.enums.ChapterFilter

class ChapterFilterTypeConverter {
    @TypeConverter
    fun filterToName(filter: ChapterFilter): String {
        return filter.name
    }

    @TypeConverter
    fun nameToFilter(name: String): ChapterFilter {
        return ChapterFilter.valueOf(name)
    }
}

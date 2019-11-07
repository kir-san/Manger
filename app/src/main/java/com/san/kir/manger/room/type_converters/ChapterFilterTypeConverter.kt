package com.san.kir.manger.room.type_converters

import androidx.room.TypeConverter
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

package com.san.kir.data.db.typeConverters

import androidx.room.TypeConverter
import com.san.kir.core.support.ChapterFilter

internal class ChapterFilterTypeConverter {
    @TypeConverter
    fun filterToName(filter: ChapterFilter): String {
        return filter.name
    }

    @TypeConverter
    fun nameToFilter(name: String): ChapterFilter {
        return ChapterFilter.valueOf(name)
    }
}

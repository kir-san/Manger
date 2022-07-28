package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter
import com.san.kir.core.support.DownloadState

internal class DownloadStateTypeConverter {
    @TypeConverter
    fun stateToString(state: DownloadState): String {
        return state.name
    }

    @TypeConverter
    fun stringToState(name: String): DownloadState {
        return DownloadState.valueOf(name)
    }
}

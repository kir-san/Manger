package com.san.kir.manger.room.type_converters

import androidx.room.TypeConverter
import com.san.kir.manger.utils.enums.DownloadState

class DownloadStateTypeConverter {
    @TypeConverter
    fun stateToString(state: DownloadState): String {
        return state.name
    }

    @TypeConverter
    fun StringToState(name: String): DownloadState {
        return DownloadState.valueOf(name)
    }
}

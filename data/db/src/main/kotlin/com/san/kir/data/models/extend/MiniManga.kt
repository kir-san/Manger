package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo

@Stable
data class MiniManga(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "isUpdate") val update: Boolean = false,
    @ColumnInfo(name = "category") val category: String = "",
)

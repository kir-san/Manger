package com.san.kir.data.models.extend

import androidx.room.ColumnInfo

data class NameAndId(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
)

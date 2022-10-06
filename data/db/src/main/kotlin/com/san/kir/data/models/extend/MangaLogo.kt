package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo

@Stable
data class MangaLogo(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "logo") val logo: String,
    @ColumnInfo(name = "path") val path: String,
)

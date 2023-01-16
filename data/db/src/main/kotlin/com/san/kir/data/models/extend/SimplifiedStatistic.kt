package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@Stable
@DatabaseView(
    viewName = "simple_statistic",
    value = "SELECT statistic.id AS id, manga.name AS manga_name, " +
            "manga.logo AS manga_logo, statistic.all_time AS all_time " +
            "FROM statistic JOIN manga ON statistic.manga_id=manga.id"
)
data class SimplifiedStatistic(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "manga_name") val name: String = "",
    @ColumnInfo(name = "manga_logo") val logo: String = "",
    @ColumnInfo(name = "all_time") val allTime: Long = 0
)

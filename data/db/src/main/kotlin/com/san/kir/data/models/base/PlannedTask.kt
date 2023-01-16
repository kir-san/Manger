package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
@Entity(tableName = "planned_task")
data class PlannedTask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0L,

    @ColumnInfo(name = "manga_id") val mangaId: Long = -1L,
    @ColumnInfo(name = "group_name") val groupName: String = "",
    @ColumnInfo(name = "group_content") val groupContent: List<String> = emptyList(),
    @ColumnInfo(name = "mangas", defaultValue = "") val mangas: List<Long> = emptyList(),
    @ColumnInfo(name = "category_id") val categoryId: Long = -1L,
    @ColumnInfo(name = "catalog") val catalog: String = "",
    @ColumnInfo(name = "type") val type: PlannedType = PlannedType.MANGA,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = false,
    @ColumnInfo(name = "period") val period: PlannedPeriod = PlannedPeriod.DAY,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: PlannedWeek = PlannedWeek.MONDAY,
    @ColumnInfo(name = "hour") val hour: Int = 0,
    @ColumnInfo(name = "minute") val minute: Int = 0,
    @ColumnInfo(name = "added_time") val addedTime: Long = 0L,
    @ColumnInfo(name = "error_message") val errorMessage: String = "",
) : Parcelable

fun PlannedTask.toBase(mangaName: String, categoryName: String): PlannedTaskBase {
    return object : PlannedTaskBase {
        override val id: Long = this@toBase.id
        override val manga: String = mangaName
        override val groupName: String = this@toBase.groupName
        override val category: String = categoryName
        override val catalog: String = this@toBase.catalog
        override val type: PlannedType = this@toBase.type
        override val period: PlannedPeriod = this@toBase.period
        override val dayOfWeek: PlannedWeek = this@toBase.dayOfWeek
        override val hour: Int = this@toBase.hour
        override val minute: Int = this@toBase.minute
    }
}

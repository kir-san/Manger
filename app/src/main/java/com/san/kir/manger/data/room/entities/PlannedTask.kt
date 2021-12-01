package com.san.kir.manger.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import com.san.kir.manger.utils.enums.PlannedPeriod
import com.san.kir.manger.utils.enums.PlannedType
import com.san.kir.manger.utils.enums.PlannedWeek
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = PlannedTaskColumn.tableName)
data class PlannedTask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = PlannedTaskColumn.id)
    var id: Long = 0L,

    @ColumnInfo(name = PlannedTaskColumn.manga)
    var manga: String = "",

    @ColumnInfo(name = PlannedTaskColumn.groupName)
    var groupName: String = "",

    @ColumnInfo(name = PlannedTaskColumn.groupContent)
    var groupContent: String = "",

    @ColumnInfo(name = PlannedTaskColumn.category)
    var category: String = "",

    @ColumnInfo(name = PlannedTaskColumn.catalog)
    var catalog: String = "",

    @ColumnInfo(name = PlannedTaskColumn.type)
    var type: PlannedType = PlannedType.MANGA,

    @ColumnInfo(name = PlannedTaskColumn.isEnabled)
    var isEnabled: Boolean = false,

    @ColumnInfo(name = PlannedTaskColumn.period)
    var period: PlannedPeriod = PlannedPeriod.DAY,

    @ColumnInfo(name = PlannedTaskColumn.dayOfWeek)
    var dayOfWeek: PlannedWeek = PlannedWeek.MONDAY,

    @ColumnInfo(name = PlannedTaskColumn.hour)
    var hour: Int = 0,

    @ColumnInfo(name = PlannedTaskColumn.minute)
    var minute: Int = 0,

    @ColumnInfo(name = PlannedTaskColumn.addedTime)
    var addedTime: Long = 0L,

    @ColumnInfo(name = PlannedTaskColumn.errorMessage)
    var errorMessage: String = "",
) : Parcelable

var PlannedTask.mangaList: List<String>
    get() = groupContent.split(",").map { it.trim() }
    set(value) {
        groupContent = value.toString().removeSurrounding("[", "]")
    }

object PlannedTaskColumn {
    const val tableName = "planned_task"

    const val id = "id"
    const val manga = "manga"
    const val groupName = "group_name"
    const val groupContent = "group_content"
    const val category = "category"
    const val catalog = "catalog"
    const val type = "type"
    const val isEnabled = "is_enabled"
    const val period = "period"
    const val dayOfWeek = "day_of_week"
    const val hour = "hour"
    const val minute = "minute"
    const val addedTime = "added_time"
    const val errorMessage = "error_message"
}





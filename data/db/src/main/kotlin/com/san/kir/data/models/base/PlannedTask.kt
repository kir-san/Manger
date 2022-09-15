package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = PlannedTask.tableName)
data class PlannedTask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Col.id)
    override var id: Long = 0L,

    @ColumnInfo(name = Col.manga)
    override var manga: String = "",

    @ColumnInfo(name = Col.groupName)
    override var groupName: String = "",

    @ColumnInfo(name = Col.groupContent)
    override var groupContent: String = "",

    @Deprecated("не использовать, так как поменяна логика на использование id")
    @ColumnInfo(name = Col.category)
    override var category: String = "",

    @ColumnInfo(name = Col.categoryId)
    override var categoryId: Long = -1L,

    @ColumnInfo(name = Col.catalog)
    override var catalog: String = "",

    @ColumnInfo(name = Col.type)
    override var type: PlannedType = PlannedType.MANGA,

    @ColumnInfo(name = Col.isEnabled)
    override var isEnabled: Boolean = false,

    @ColumnInfo(name = Col.period)
    override var period: PlannedPeriod = PlannedPeriod.DAY,

    @ColumnInfo(name = Col.dayOfWeek)
    override var dayOfWeek: PlannedWeek = PlannedWeek.MONDAY,

    @ColumnInfo(name = Col.hour)
    override var hour: Int = 0,

    @ColumnInfo(name = Col.minute)
    override var minute: Int = 0,

    @ColumnInfo(name = Col.addedTime)
    override var addedTime: Long = 0L,

    @ColumnInfo(name = Col.errorMessage)
    override var errorMessage: String = "",
) : PlannedTaskBase, Parcelable {
    companion object {
        const val tableName = "planned_task"
    }

    object Col {
        const val id = "id"
        const val manga = "manga"
        const val groupName = "group_name"
        const val groupContent = "group_content"
        const val category = "category"
        const val categoryId = "category_id"
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
}

var PlannedTask.mangaList: List<String>
    get() = groupContent.split(",").map { it.trim() }
    set(value) {
        groupContent = value.toString().removeSurrounding("[", "]")
    }



package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Ignore
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.base.PlannedTaskBase

// Расширение PlannedTask для получения названия категории из вложенного запроса
@DatabaseView(
    viewName = PlannedTaskExt.viewName,
    value = "SELECT " +
            "${PlannedTask.tableName}.${PlannedTask.Col.id}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.manga}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.groupName}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.groupContent}, " +

            "(SELECT name FROM categories " +
            "WHERE ${PlannedTask.tableName}.${PlannedTask.Col.categoryId} =" +
            " categories.id) " +
            "AS ${PlannedTask.Col.category}, " +

            "${PlannedTask.tableName}.${PlannedTask.Col.categoryId}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.catalog}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.type}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.isEnabled}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.period}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.dayOfWeek}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.hour}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.minute}, " +
            "${PlannedTask.tableName}.${PlannedTask.Col.addedTime} " +

            "FROM ${PlannedTask.tableName}"
)
data class PlannedTaskExt(
    @ColumnInfo(name = PlannedTask.Col.id)
    override val id: Long = 0L,

    @ColumnInfo(name = PlannedTask.Col.manga)
    override val manga: String = "",

    @ColumnInfo(name = PlannedTask.Col.groupName)
    override val groupName: String = "",

    @ColumnInfo(name = PlannedTask.Col.groupContent)
    override val groupContent: String = "",

    @ColumnInfo(name = PlannedTask.Col.category)
    override val category: String? = "",

    @ColumnInfo(name = PlannedTask.Col.categoryId)
    override val categoryId: Long = -1L,

    @ColumnInfo(name = PlannedTask.Col.catalog)
    override val catalog: String = "",

    @ColumnInfo(name = PlannedTask.Col.type)
    override val type: PlannedType = PlannedType.MANGA,

    @ColumnInfo(name = PlannedTask.Col.isEnabled)
    override val isEnabled: Boolean = false,

    @ColumnInfo(name = PlannedTask.Col.period)
    override val period: PlannedPeriod = PlannedPeriod.DAY,

    @ColumnInfo(name = PlannedTask.Col.dayOfWeek)
    override val dayOfWeek: PlannedWeek = PlannedWeek.MONDAY,

    @ColumnInfo(name = PlannedTask.Col.hour)
    override val hour: Int = 0,

    @ColumnInfo(name = PlannedTask.Col.minute)
    override val minute: Int = 0,

    @ColumnInfo(name = PlannedTask.Col.addedTime)
    override val addedTime: Long = 0L,
) : PlannedTaskBase {
    companion object {
        const val viewName = "planned_task_ext"
    }

    @Ignore
    override val errorMessage: String = ""
}

package com.san.kir.manger.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.san.kir.manger.utils.enums.PlannedPeriod
import com.san.kir.manger.utils.enums.PlannedType

@Entity(tableName = PlannedTaskColumn.tableName)
class PlannedTask() : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = PlannedTaskColumn.id)
    var id = 0L
    @ColumnInfo(name = PlannedTaskColumn.manga)
    var manga = ""
    @ColumnInfo(name = PlannedTaskColumn.groupName)
    var groupName = ""
    @ColumnInfo(name = PlannedTaskColumn.groupContent)
    var groupContent = ""
    @ColumnInfo(name = PlannedTaskColumn.category)
    var category = ""
    @ColumnInfo(name = PlannedTaskColumn.catalog)
    var catalog = ""
    @ColumnInfo(name = PlannedTaskColumn.type)
    var type = PlannedType.MANGA
    @ColumnInfo(name = PlannedTaskColumn.isEnabled)
    var isEnabled = false
    @ColumnInfo(name = PlannedTaskColumn.period)
    var period = PlannedPeriod.DAY
    @ColumnInfo(name = PlannedTaskColumn.dayOfWeek)
    var dayOfWeek = 0
    @ColumnInfo(name = PlannedTaskColumn.hour)
    var hour = 0
    @ColumnInfo(name = PlannedTaskColumn.minute)
    var minute = 0
    @ColumnInfo(name = PlannedTaskColumn.addedTime)
    var addedTime = 0L
    @ColumnInfo(name = PlannedTaskColumn.errorMessage)
    var errorMessage = ""

    @Ignore
    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        manga = parcel.readString() ?: ""
        groupName = parcel.readString() ?: ""
        groupContent = parcel.readString() ?: ""
        category = parcel.readString() ?: ""
        catalog = parcel.readString() ?: ""
        type = parcel.readInt()
        isEnabled = parcel.readByte() != 0.toByte()
        period = parcel.readInt()
        dayOfWeek = parcel.readInt()
        hour = parcel.readInt()
        minute = parcel.readInt()
        addedTime = parcel.readLong()
        errorMessage = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(manga)
        parcel.writeString(groupName)
        parcel.writeString(groupContent)
        parcel.writeString(category)
        parcel.writeString(catalog)
        parcel.writeInt(type)
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeInt(period)
        parcel.writeInt(dayOfWeek)
        parcel.writeInt(hour)
        parcel.writeInt(minute)
        parcel.writeLong(addedTime)
        parcel.writeString(errorMessage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlannedTask> {
        override fun createFromParcel(parcel: Parcel): PlannedTask {
            return PlannedTask(parcel)
        }

        override fun newArray(size: Int): Array<PlannedTask?> {
            return arrayOfNulls(size)
        }
    }

}

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





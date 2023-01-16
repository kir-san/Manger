package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.DownloadState
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "catalog_task")
data class CatalogTask(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) override val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "state") override val state: DownloadState = DownloadState.QUEUED,
    @ColumnInfo(name = "progress") val progress: Float = 0f,
) : Parcelable, BaseTask<CatalogTask> {
    override fun setPaused() = copy(state = DownloadState.PAUSED)
}

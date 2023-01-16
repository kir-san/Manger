package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.DownloadState
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "manga_task")
data class MangaTask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") override val id: Long = 0L,
    @ColumnInfo(name = "manga_id") val mangaId: Long = 0L,
    @ColumnInfo(name = "manga_name") val mangaName: String = "",
    @ColumnInfo(name = "new_chapters") val newChapters: Int = 0,
    @ColumnInfo(name = "state") override val state: DownloadState = DownloadState.QUEUED,
) : Parcelable, BaseTask<MangaTask> {
    override fun setPaused() = copy(state = DownloadState.PAUSED)
}

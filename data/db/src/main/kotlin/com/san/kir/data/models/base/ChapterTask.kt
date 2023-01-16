package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.DownloadState
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "chapter_task")
data class ChapterTask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") override val id: Long = 0L,
    @ColumnInfo(name = "chapter_id") val chapterId: Long = 0L,
    @ColumnInfo(name = "state") override val state: DownloadState = DownloadState.QUEUED,
    val chapterName: String = "",
    val max: Int = 0,
    val progress: Int = 0,
    val size: Long = 0,
    val time: Long = 0,
) : Parcelable, BaseTask<ChapterTask> {
    override fun setPaused() = copy(state = DownloadState.PAUSED)
}

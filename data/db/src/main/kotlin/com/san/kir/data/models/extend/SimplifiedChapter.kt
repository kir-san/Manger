package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.core.support.DownloadState

@DatabaseView(
    viewName = "simple_chapter",
    value = "SELECT " +
            "chapters.id, " +
            "chapters.status, " +

            "IIF(chapters.totalPages=0, " +
            "chapters.totalPages,  " +
            "chapters.downloadPages * 100/ chapters.totalPages) AS progress, " +

            "chapters.isRead, " +
            "chapters.pages, " +
            "chapters.name, " +
            "manga.name AS manga, " +
            "chapters.date " +
            "FROM chapters JOIN manga ON chapters.manga_id=manga.id " +
            "WHERE chapters.isInUpdate IS 1 ORDER BY chapters.id DESC"
)
@Stable
data class SimplifiedChapter(
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "status")
    val status: DownloadState,

    @ColumnInfo(name = "progress")
    val progress: Int,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean,

    @ColumnInfo(name = "pages")
    val pages: List<String>,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "manga")
    val manga: String,

    @ColumnInfo(name = "date")
    val date: String
)

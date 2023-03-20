package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.getCountPagesForChapterInMemory
import com.san.kir.data.models.utils.compareChapterNames
import com.san.kir.data.models.utils.preparePath

@DatabaseView(
    viewName = "simple_chapter",
    value = "SELECT " +
            "chapters.id, " +
            "chapters.status, " +
            "chapters.progress, " +
            "chapters.isRead, " +
            "chapters.downloadPages, " +
            "chapters.pages, " +
            "chapters.name, " +
            "manga.name AS manga, " +
            "chapters.date, " +
            "chapters.path " +
            "FROM chapters JOIN manga ON chapters.manga_id=manga.id " +
            "WHERE chapters.isInUpdate IS 1 ORDER BY chapters.id DESC"
)
@Stable
data class SimplifiedChapter(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "status") val status: DownloadState,
    @ColumnInfo(name = "progress") val progress: Int,
    @ColumnInfo(name = "isRead") val isRead: Boolean,
    @ColumnInfo(name = "downloadPages") val downloadPages: Int,
    @ColumnInfo(name = "pages") val pages: List<String>,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "manga") val manga: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "path") val path: String,
) {
    val downloadProgress: Int
        get() = if (pages.isEmpty()) 0 else downloadPages * 100 / pages.size
}

val SimplifiedChapter.countPages: Int get() = getCountPagesForChapterInMemory(path.preparePath())

class SimplifiedChapterComparator : Comparator<SimplifiedChapter> {
    override fun compare(o1: SimplifiedChapter, o2: SimplifiedChapter) =
        compareChapterNames(o1.name, o2.name)
}

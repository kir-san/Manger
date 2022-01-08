package com.san.kir.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.getCountPagesForChapterInMemory
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.isEmptyDirectory
import kotlinx.parcelize.Parcelize

@Entity(tableName = Chapter.tableName)
@Parcelize
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Col.id)
    var id: Long = 0,

    @ColumnInfo(name = Col.manga)
    var manga: String = "",

    @ColumnInfo(name = Col.mangaId, defaultValue = "0")
    var mangaId: Long = 0,

    @ColumnInfo(name = Col.name)
    var name: String = "",

    @ColumnInfo(name = Col.date)
    var date: String = "",

    @ColumnInfo(name = Col.path)
    var path: String = "",

    @ColumnInfo(name = Col.isRead)
    var isRead: Boolean = false,

    @ColumnInfo(name = Col.link)
    var link: String = "",

    @ColumnInfo(name = Col.progress)
    var progress: Int = 0,

    @ColumnInfo(name = Col.pages)
    var pages: List<String> = listOf(),

    @ColumnInfo(name = Col.isInUpdate)
    var isInUpdate: Boolean = false,

    @ColumnInfo(name = Col.totalPages)
    var totalPages: Int = 0,

    @ColumnInfo(name = Col.downloadPages)
    var downloadPages: Int = 0,

    @ColumnInfo(name = Col.totalSize)
    var totalSize: Long = 0L,

    @ColumnInfo(name = Col.downloadSize)
    var downloadSize: Long = 0L,

    @ColumnInfo(name = Col.totalTime)
    var totalTime: Long = 0L,

    @ColumnInfo(name = Col.status)
    var status: DownloadState = DownloadState.UNKNOWN,

    @ColumnInfo(name = Col.order)
    var order: Long = 0,

    @ColumnInfo(name = Col.error)
    var isError: Boolean = false
) : Parcelable {
    companion object {
        const val tableName = "chapters"
    }

    object Col {
        const val id = "id"
        const val manga = "manga"
        const val mangaId = "manga_id"
        const val name = "name"
        const val date = "date"
        const val path = "path"
        const val isRead = "isRead"
        const val link = "site"
        const val progress = "progress"
        const val pages = "pages"
        const val isInUpdate = "isInUpdate"
        const val totalPages = "totalPages"
        const val downloadPages = "downloadPages"
        const val totalSize = "totalSize"
        const val downloadSize = "downloadSize"
        const val totalTime = "totalTime"
        const val status = "status"
        const val order = "ordering"
        const val error = "error"
    }
}

val Chapter.countPages: Int get() = getCountPagesForChapterInMemory(path)

val Chapter.action: Int
    get() {  // Определение доступного действия для главы
        getFullPath(path).apply {
            when {
                // если ссылка есть и если папка пуста или папки нет, то можно скачать
                link.isNotEmpty() && (isEmptyDirectory || !exists()) -> return ChapterStatus.DOWNLOADABLE
                // если папка непустая, то статус соответствует удалению
                !isEmptyDirectory -> return ChapterStatus.DELETE
                // папка не существет и ссылки на загрузку нет, то больше ничего не сделаешь
                !exists() and link.isEmpty() -> return ChapterStatus.NOT_LOADED
            }
        }
        return ChapterStatus.UNKNOWN // такого быть не должно, но если случится дайте знать
    }


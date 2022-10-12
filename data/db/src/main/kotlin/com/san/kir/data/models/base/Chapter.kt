package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.getCountPagesForChapterInMemory
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.isEmptyDirectory
import kotlinx.parcelize.Parcelize

@Stable
@Entity(tableName = "chapters")
@Parcelize
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @Deprecated("not use", ReplaceWith("mangaId"))
    @ColumnInfo(name = "manga")
    val manga: String = "",

    @ColumnInfo(name = "manga_id", defaultValue = "0")
    val mangaId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "date")
    val date: String = "",

    @ColumnInfo(name = "path")
    val path: String = "",

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = false,

    @ColumnInfo(name = "site")
    val link: String = "",

    @ColumnInfo(name = "progress")
    val progress: Int = 0,

    @ColumnInfo(name = "pages")
    val pages: List<String> = listOf(),

    @ColumnInfo(name = "isInUpdate")
    val isInUpdate: Boolean = false,

    @ColumnInfo(name = "totalPages")
    val totalPages: Int = 0,

    @ColumnInfo(name = "downloadPages")
    val downloadPages: Int = 0,

    @ColumnInfo(name = "totalSize")
    val totalSize: Long = 0L,

    @ColumnInfo(name = "downloadSize")
    val downloadSize: Long = 0L,

    @ColumnInfo(name = "totalTime")
    val totalTime: Long = 0L,

    @ColumnInfo(name = "status")
    val status: DownloadState = DownloadState.UNKNOWN,

    @ColumnInfo(name = "ordering")
    val order: Long = 0,

    @ColumnInfo(name = "error")
    val isError: Boolean = false
) : Parcelable

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


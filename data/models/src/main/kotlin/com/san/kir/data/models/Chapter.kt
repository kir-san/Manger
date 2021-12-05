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
import com.san.kir.data.models.columns.ChaptersColumn
import kotlinx.parcelize.Parcelize

@Entity(tableName = ChaptersColumn.tableName)
@Parcelize
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ChaptersColumn.id)
    var id: Long = 0,

    @ColumnInfo(name = ChaptersColumn.manga)
    var manga: String = "",

    @ColumnInfo(name = ChaptersColumn.name)
    var name: String = "",

    @ColumnInfo(name = ChaptersColumn.date)
    var date: String = "",

    @ColumnInfo(name = ChaptersColumn.path)
    var path: String = "",

    @ColumnInfo(name = ChaptersColumn.isRead)
    var isRead: Boolean = false,

    @ColumnInfo(name = ChaptersColumn.site)
    var link: String = "",

    @ColumnInfo(name = ChaptersColumn.progress)
    var progress: Int = 0,

    @ColumnInfo(name = ChaptersColumn.pages)
    var pages: List<String> = listOf(),

    @ColumnInfo(name = ChaptersColumn.isInUpdate)
    var isInUpdate: Boolean = false,

    @ColumnInfo(name = ChaptersColumn.totalPages)
    var totalPages: Int = 0,

    @ColumnInfo(name = ChaptersColumn.downloadPages)
    var downloadPages: Int = 0,

    @ColumnInfo(name = ChaptersColumn.totalSize)
    var totalSize: Long = 0L,

    @ColumnInfo(name = ChaptersColumn.downloadSize)
    var downloadSize: Long = 0L,

    @ColumnInfo(name = ChaptersColumn.totalTime)
    var totalTime: Long = 0L,

    @ColumnInfo(name = ChaptersColumn.status)
    var status: DownloadState = DownloadState.UNKNOWN,

    @ColumnInfo(name = ChaptersColumn.order)
    var order: Long = 0,

    @ColumnInfo(name = ChaptersColumn.error)
    var isError: Boolean = false
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


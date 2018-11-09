package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.san.kir.manger.utils.ChapterStatus
import com.san.kir.manger.utils.getCountPagesForChapterInMemory
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isEmptyDirectory

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var manga: String = "",
    var name: String = "",
    var date: String = "",
    var path: String = "",
    var isRead: Boolean = false,
    var site: String = "",
    var progress: Int = 0,
    var pages: List<String> = listOf()
)

val Chapter.countPages: Int get() = getCountPagesForChapterInMemory(path)

val Chapter.action: Int
    get() {  // Определение доступного действия для главы
        getFullPath(path).apply {
            when {
                // если ссылка есть и если папка пуста или папки нет, то можно скачать
                site.isNotEmpty() && (isEmptyDirectory || !exists()) -> return ChapterStatus.DOWNLOADABLE
                // если папка непустая, то статус соответствует удалению
                !isEmptyDirectory -> return ChapterStatus.DELETE
                // папка не существет и ссылки на загрузку нет, то больше ничего не сделаешь
                !exists() and site.isEmpty() -> return ChapterStatus.NOT_LOADED
            }
        }
        return ChapterStatus.UNKNOWN // такого быть не должно, но если случится дайте знать
    }

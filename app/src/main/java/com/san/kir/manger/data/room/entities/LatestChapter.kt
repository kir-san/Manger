package com.san.kir.manger.data.room.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.isEmptyDirectory

@Entity(tableName = "latestChapters")
class LatestChapter {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var manga = ""
    var name = ""
    var date = ""
    var path = ""
    var site = ""

     constructor()
    @Ignore constructor(chapter: Chapter) {
        manga = chapter.manga
        name = chapter.name
        date = chapter.date
        site = chapter.link
        path = chapter.path
    }
}

val LatestChapter.action: Int
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

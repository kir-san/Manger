package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.dateFormat
import com.san.kir.manger.utils.getCountPagesForChapterInMemory
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isEmptyDirectory
import java.util.*

@Entity(tableName = "chapters")
class Chapter {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var manga = ""
    var name = ""
    var date = ""
    var path = ""
    var isRead = false
    var site = ""
    var progress = 0

    constructor()
    constructor(manga: String,
                name: String,
                date: String = dateFormat.format(Date()),
                path: String = "",
                isRead: Boolean = false,
                site: String = "",
                progress: Int = 0) {
        this.manga = manga
        this.name = name
        this.date = date
        this.path = path
        this.isRead = isRead
        this.site = site
        this.progress = progress
    }
}

val Chapter.countPages: Int get() = getCountPagesForChapterInMemory(path)

val Chapter.action: Int get() {  // Определение доступного действия для главы
    getFullPath(path).apply {
        when {
        // если ссылка есть и если папка пуста или папки нет, то можно скачать
            site.isNotEmpty() && (isEmptyDirectory || !exists()) -> return CHAPTER_STATUS.DOWNLOADABLE
        // если папка непустая, то статус соответствует удалению
            !isEmptyDirectory -> return CHAPTER_STATUS.DELETE
        // папка не существет и ссылки на загрузку нет, то больше ничего не сделаешь
            !exists() and site.isEmpty() -> return CHAPTER_STATUS.NOT_LOADED
        }
    }
    return CHAPTER_STATUS.UNKNOW // такого быть не должно, но если случится дайте знать
}

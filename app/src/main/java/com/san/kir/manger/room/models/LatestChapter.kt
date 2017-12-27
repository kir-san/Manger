package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isEmptyDirectory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.async

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
    constructor(chapter: Chapter) {
        manga = chapter.manga
        name = chapter.name
        date = chapter.date
        site = chapter.site
        path = chapter.path
    }
}

val LatestChapter.isRead
    get() = async {
        try {
            Main.db.chapterDao.loadChapters(manga)
                    .first { it.name == name }
                    .isRead
        } catch (ex: NoSuchElementException) {
            log("error on $manga $name")
            false
        }
    }

val LatestChapter.action: Int
    get() {  // Определение доступного действия для главы
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

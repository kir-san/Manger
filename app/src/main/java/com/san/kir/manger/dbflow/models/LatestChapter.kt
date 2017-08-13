package com.san.kir.manger.dbflow.models

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.san.kir.manger.dbflow.AppDatabase
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isEmptyDirectory

@Table(name = "latestChapters", database = AppDatabase::class)
class LatestChapter : BaseModel {
    @PrimaryKey(autoincrement = true) @Column var id: Long = 0
    @Column var manga: String = String()
    @Column var name: String = String()
    @Column var date: String = String()
    @Column var site: String = String()
    @Column var path: String = String()
    var isRead: Boolean = false

    val action: Int get() {  // Определение доступного действия для главы
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

    constructor() // not delete
    constructor(chapter: Chapter) {
        manga = chapter.manga
        name = chapter.name
        date = chapter.date
        site = chapter.site
        path = chapter.path
    }
}

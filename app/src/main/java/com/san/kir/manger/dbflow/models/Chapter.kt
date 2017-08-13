package com.san.kir.manger.dbflow.models

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.san.kir.manger.dbflow.AppDatabase
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.dateFormat
import com.san.kir.manger.utils.getCountPagesForChapterInMemory
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isEmptyDirectory
import java.util.*

@Table(name = "chapters", database = AppDatabase::class)
open class Chapter : BaseModel {
    @PrimaryKey(autoincrement = true) @Column var id: Long = 0
    @Column var manga: String = String()
    @Column var name: String = String()
    @Column var date: String = String()
    @Column var path: String = String()
    @Column var isRead: Boolean = false
    @Column var site: String = String()
    @Column var progress: Int = 0

    val countPages: Int get() = getCountPagesForChapterInMemory(path)

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

    constructor()

    constructor(manga: String,
                name: String,
                date: String = dateFormat.format(Date()),
                path: String = String(),
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

    fun updateProgress(progress: Int) {
        this.progress = progress
        this.update()
    }

    fun updateStatus(status: Boolean) {
        this.isRead = status
        this.update()
    }
}

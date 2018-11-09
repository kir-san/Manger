package com.san.kir.manger.components.viewer


import android.os.Parcel
import android.os.Parcelable
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.utils.getFullPath

// класс для управления страницами и главами
class ChaptersList(
    mangaName: String,
    chapter: String
//    val act: ViewerActivity
) {
    var page = PageObject
    var chapter = ChapterObject

    init {
        Helper.list_chapter.clear() // Очистить список
        Helper.list_chapter.addAll(Helper.chapterDao.loadChapters(mangaName)) // Получение глав
        Helper.position_chapter = findChapterPosition(chapter) // Установка текущей главы

        PageObject.updateList() // Получение списка страниц для главы
        PageObject.position = when { // Установка текущей страницы
            ChapterObject.current.progress <= 0 -> 0 // Если не больше 0, то ноль
            else -> ChapterObject.current.progress // Иначе как есть
        }

        Helper.positionStat = PageObject.position
        Helper.stats = Helper.statisticDao.loadItem(mangaName)
        Helper.stats.lastChapters = 0
        Helper.stats.lastPages = 0
        Helper.statisticDao.update(Helper.stats)
    }

    private fun findChapterPosition(chapter: String): Int { // Позиции главы, по названию главы
        val lastIndex = Helper.list_chapter.size - 1 // Последняя позиция
        // Проверка всех названий глав на соответствие, если ничего нет, то позиция равна 0
        return (0..lastIndex).firstOrNull { Helper.list_chapter[it].name == chapter } ?: 0
    }

    object Helper { // маленький объект
        val chapterDao = Main.db.chapterDao
        val statisticDao = Main.db.statisticDao
        val list_chapter: MutableList<Chapter> = mutableListOf() // Список глав
        var position_chapter = 0 // текущая глава
        var position_page = 0 // текущая страница
        var stats = MangaStatistic()
        var positionStat = 0
    }

    object ChapterObject { // группа для глав
        val position: Int // текущая глава
            get() = Helper.position_chapter + 1

        val max: Int // Общее количество глав
            get() = Helper.list_chapter.size

        val current: Chapter // Текущая глава
            get() = Helper.list_chapter[Helper.position_chapter]

        fun next() { // переключение на следующую главу
            if (hasNext()) {
                Helper.position_chapter++
                PageObject.updateList()
                Helper.stats.lastChapters++
                Helper.stats.allChapters++
                Helper.statisticDao.update(Helper.stats)
            }
        }

        fun hasNext(): Boolean {
            return Helper.position_chapter < Helper.list_chapter.size - 1
        }

        fun prev() { // переключение на предыдущию главу
            if (hasPrev()) {
                Helper.position_chapter--
                PageObject.updateList()
            }
        }

        fun hasPrev(): Boolean {
            return Helper.position_chapter > 0
        }
    }

    object PageObject { // группа для страниц
        var position: Int // текущая страница
            get() = Helper.position_page
            set(value) {
                Helper.position_page = value
                saveProgress(value) // Сохранить позицию в бд
            }

        var max: Int = 0 // Количество глав
            private set

        var list: List<Page> = listOf()
            private set

        private fun saveProgress(pos: Int = position) { // Сохранение позиции текущей главы
            var p = pos // скопировать позицию
            when {
                pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
                pos == max -> { // если текущая позиция последняя
                    p = max
                    // Сделать главу прочитанной
                    ChapterObject.current.isRead = true
                    Helper.chapterDao.update(ChapterObject.current)
                }
                pos > max -> return // Если больше максимального значения, ничего не делать
            }
            // Обновить позицию
            ChapterObject.current.progress = p
            Helper.chapterDao.update(ChapterObject.current)

            if (pos > Helper.positionStat) {
                val diff = pos - Helper.positionStat
                Helper.stats.lastPages += diff
                Helper.stats.allPages += diff
                Helper.statisticDao.update(Helper.stats)
                Helper.positionStat = pos
            }
        }

        fun updateList() {
            if (ChapterObject.current.pages.isNullOrEmpty() ||
                ChapterObject.current.pages.any { it.isBlank() }) {
                ChapterObject.current.pages = ManageSites.pages(ChapterObject.current)
                Helper.chapterDao.update(ChapterObject.current)
            }

            max = ChapterObject.current.pages.size

            val pages = ChapterObject.current.pages.map {
                Page(it, getFullPath(ChapterObject.current.path).absolutePath)
            }.toMutableList()

            if (ChapterObject.hasPrev())  // Если есть главы до этой
                pages.add(0, Page("prev")) // Добавить в начало специальный файл указатель
            else  // если нет
                pages.add(0, Page("none")) // Добавить в начало другой файл указатель

            if (ChapterObject.hasNext())  // Если есть главы после этой
                pages.add(Page("next")) // Добавить в конец специальный файл указатель
            else // если нет
                pages.add(Page("none")) // Добавить в конец другой файл указатель

            Helper.positionStat = 1

            list = pages
        }
    }
}

data class Page(val link: String, val fullPath: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(link)
        parcel.writeString(fullPath)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Page> {
        override fun createFromParcel(parcel: Parcel) = Page(parcel)

        override fun newArray(size: Int): Array<Page?> = arrayOfNulls(size)
    }
}

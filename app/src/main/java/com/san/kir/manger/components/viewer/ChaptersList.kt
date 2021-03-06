package com.san.kir.manger.components.viewer


import android.os.Parcel
import android.os.Parcelable
import com.san.kir.manger.components.list_chapters.ChapterComparator
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.utils.extensions.getFullPath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// класс для управления страницами и главами
class ChaptersList(private val act: ViewerActivity) {
    private val pool = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private var listChapter: List<Chapter> = listOf() // Список глав
    private var positionChapter = 0 // текущая глава
    private var stats = MangaStatistic()
    private var positionStat = 0

    suspend fun init(chapter: Chapter, isAlternative: Boolean) {
        val list = act.mViewModel.getChapterItems(chapter.manga)

        listChapter = if (isAlternative) {
            list.sortedWith(ChapterComparator())
        } else {
            list
        }

        positionChapter = findChapterPosition(chapter.name) // Установка текущей главы

        updatePagesList() // Получение списка страниц для главы
        pagePosition = when { // Установка текущей страницы
            chapter().progress <= 0 -> 0 // Если не больше 0, то ноль
            else -> chapter().progress // Иначе как есть
        }

        positionStat = pagePosition

        stats = act.mViewModel.getStatisticItem(chapter.manga)
        stats.lastChapters = 0
        stats.lastPages = 0

        act.mViewModel.statisticUpdate(stats)
    }

    var pagePosition: Int = 0 // текущая страница
        set(value) {
            field = value
            GlobalScope.launch(pool) {
                saveProgress(value) // Сохранить позицию в бд
            }
        }

    var pagesSize: Int = 0 // Количество глав
        private set

    var pagesList: List<Page> = listOf()
        private set

    val chapterPosition: Int // текущая глава
        get() = positionChapter + 1

    val chaptersSize: Int // Общее количество глав
        get() = listChapter.size

    fun chapter(): Chapter {
        return listChapter[positionChapter]
    }

    suspend fun nextChapter() { // переключение на следующую главу
        if (hasNextChapter()) {
            positionChapter++
            updatePagesList()
            stats.lastChapters++
            stats.allChapters++
            act.mViewModel.statisticUpdate(stats)
        }
    }

    suspend fun prevChapter() { // переключение на предыдущию главу
        if (hasPrevChapter()) {
            positionChapter--
            updatePagesList()
        }
    }

    private fun findChapterPosition(chapter: String): Int { // Позиции главы, по названию главы
        val lastIndex = listChapter.size - 1 // Последняя позиция
        // Проверка всех названий глав на соответствие, если ничего нет, то позиция равна 0
        return (0..lastIndex).firstOrNull { listChapter[it].name == chapter } ?: 0
    }

    private suspend fun updatePagesList() {
        if (chapter().pages.isNullOrEmpty() ||
            chapter().pages.any { it.isBlank() }) {
            chapter().pages = ManageSites.pages(chapter())
            act.mViewModel.update(chapter())
        }

        pagesSize = chapter().pages.size

        val pages = chapter().pages.map {
            Page(it, getFullPath(chapter().path).absolutePath)
        }.toMutableList()

        if (hasPrevChapter())  // Если есть главы до этой
            pages.add(0, Page("prev")) // Добавить в начало специальный файл указатель
        else  // если нет
            pages.add(0, Page("none")) // Добавить в начало другой файл указатель

        if (hasNextChapter())  // Если есть главы после этой
            pages.add(Page("next")) // Добавить в конец специальный файл указатель
        else // если нет
            pages.add(Page("none")) // Добавить в конец другой файл указатель

        positionStat = 1

        pagesList = pages
    }

    private suspend fun saveProgress(pos: Int) { // Сохранение позиции текущей главы
        var p = pos // скопировать позицию
        when {
            pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
            pos == pagesSize -> { // если текущая позиция последняя
                p = pagesSize
                // Сделать главу прочитанной
                chapter().isRead = true
                act.mViewModel.update(chapter())
            }
            pos > pagesSize -> return // Если больше максимального значения, ничего не делать
        }
        // Обновить позицию
        chapter().progress = p
        act.mViewModel.update(chapter())

        if (pos > positionStat) {
            val diff = pos - positionStat
            stats.lastPages += diff
            stats.allPages += diff
            positionStat = pos
            act.mViewModel.statisticUpdate(stats)
        }
    }

    private fun hasNextChapter() = positionChapter < listChapter.size - 1

    private fun hasPrevChapter() = positionChapter > 0
}

data class Page(val link: String, val fullPath: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
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

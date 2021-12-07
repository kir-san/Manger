package com.san.kir.ui.viewer

import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.Manga
import com.san.kir.data.models.MangaStatistic
import com.san.kir.data.models.utils.ChapterComparator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// класс для управления страницами и главами
internal class ChaptersManager @Inject constructor(
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
) {

    // Вспомогательная переменная для расчета количества прочитанных страниц за сессию
    private var staticticPosition = 0
    private var statisticItem = MangaStatistic()

    private val _chapters = MutableStateFlow(listOf<Chapter>()) // Список глав
    val chapters = _chapters.asStateFlow()

    private val _positionChapter = MutableStateFlow(0) // Позиция текущей глава
    val chapterPosition = _positionChapter.map { it + 1 } // Позиция текущей главы для ui

    private val _currentChapter = MutableStateFlow(Chapter())
    val currentChapter = _currentChapter.asStateFlow() // Текущая глава

    private val _pages = MutableStateFlow(listOf<Page>())
    val pages = _pages.asStateFlow() // Список подготовленных страниц

    private val _pagePosition = MutableStateFlow(-1)
    val pagePosition = _pagePosition.asStateFlow()
    fun updatePagePosition(position: Int) {
        _pagePosition.value = position
    }

    suspend fun init(manga: Manga, chapterId: Long) = withDefaultContext {
        val list = chapterDao.getItemsWhereManga(manga.name)

        combine(_chapters, _positionChapter) { l, i ->
            if (l.isEmpty()) Chapter()
            else l[i]
        }
            .onEach { chapter -> _currentChapter.update { chapter } }
            .launchIn(this)

        _chapters.update {
            if (manga.isAlternativeSort) {
                list.sortedWith(ChapterComparator())
            } else {
                list
            }
        }

        _positionChapter.update { // Установка текущей главы
            findChapterPosition(_chapters.value, chapterId)
        }

        updatePages() // Получение списка страниц для главы

        _pagePosition.value =
            when { // Установка текущей страницы
                currentChapter.value.progress <= 1 -> 1 // Если не больше 0, то ноль
                else -> currentChapter.value.progress // Иначе как есть
            }

        staticticPosition = pagePosition.value

        statisticItem = statisticDao.getItem(manga.name)
        statisticItem.lastChapters = 0
        statisticItem.lastPages = 0
        statisticDao.update(statisticItem)


        pagePosition.onEach { saveProgress(it) }.launchIn(this)
    }

    fun nextPage() {
        _pagePosition.update { it + 1 }
    }

    fun prevPage() {
        _pagePosition.update { it - 1 }
    }

    suspend fun nextChapter() { // переключение на следующую главу
        if (hasNextChapter()) {
            _positionChapter.update { it + 1 }
            updatePages()
            statisticItem.lastChapters++
            statisticItem.allChapters++
            statisticDao.update(statisticItem)
        }
    }

    suspend fun prevChapter() { // переключение на предыдущию главу
        if (hasPrevChapter()) {
            _positionChapter.update { it - 1 }
            updatePages()
        }
    }

    private fun findChapterPosition(
        chapters: List<Chapter>,
        chapterId: Long,
    ): Int { // Позиции главы, по названию главы
        val lastIndex = chapters.size - 1 // Последняя позиция
        // Проверка всех названий глав на соответствие, если ничего нет, то позиция равна 0
        return (0..lastIndex).firstOrNull { chapters[it].id == chapterId } ?: 0
    }

    private suspend fun updatePages() {
//        if (currentChapter.value.pages.isNullOrEmpty() ||
//            currentChapter.value.pages.any { it.isBlank() }
//        ) {
////            chapter().pages = SiteCatalogsManager.pages(chapter())
//            chapterDao.update(currentChapter.value)
//        }

        val pages = mutableListOf<Page>()

        if (hasPrevChapter())  // Если есть главы до этой
            pages.add(Page.Prev) // Добавить в конец указатель наличия предыдущей главы
        else  // если нет
            pages.add(Page.NonePrev) // Добавить в конец указатель отсутствия предыдущей главы

        pages.addAll(
            currentChapter.value.pages
                .map<String, Page> { Page.Current(it, currentChapter.value) })

        if (hasNextChapter())  // Если есть главы после этой
            pages.add(Page.Next) // Добавить в конец указатель наличия следующей главы
        else // если нет
            pages.add(Page.NoneNext) // Добавить в конец указатель отсутствия следующей главы

        log(pages.toString())

        _pages.update { pages }
        _pagePosition.value = 1

        staticticPosition = 1

    }

    private suspend fun saveProgress(pos: Int) { // Сохранение позиции текущей главы
        var p = pos // скопировать позицию
        val chapter = currentChapter.value

        when {
            pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
            pos == pages.value.size -> { // если текущая позиция последняя
                p = pages.value.size
                // Сделать главу прочитанной

                chapter.isRead = true
                chapterDao.update(chapter)
            }
            pos > pages.value.size -> return // Если больше максимального значения, ничего не делать
        }
        // Обновить позицию
        chapter.progress = p
        chapterDao.update(chapter)

        if (pos > staticticPosition) {
            val diff = pos - staticticPosition
            statisticItem.lastPages += diff
            statisticItem.allPages += diff
            staticticPosition = pos
            statisticDao.update(statisticItem)
        }
    }

    private fun hasNextChapter() = _positionChapter.value < chapters.value.size - 1

    private fun hasPrevChapter() = _positionChapter.value > 0
}

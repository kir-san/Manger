package com.san.kir.ui.viewer

import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.Manga
import com.san.kir.data.models.MangaStatistic
import com.san.kir.data.models.utils.ChapterComparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// класс для управления страницами и главами
internal class ChaptersManager @Inject constructor(
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
) {

    private val pagePosition = MutableStateFlow(-1)

    // Вспомогательная переменная для расчета количества прочитанных страниц за сессию
    private var staticticPosition = 0
    private var statisticItem = MangaStatistic()

    fun updateStatisticData(downloadSize: Long, downloadTime: Long) {
        if (downloadSize > 100L) {
            statisticItem.lastDownloadSize += downloadSize
            statisticItem.downloadSize += downloadSize
            statisticItem.lastDownloadTime += downloadTime
            statisticItem.downloadTime += downloadTime
        }
    }

    suspend fun updatePagePosition(position: Int) {
        if (currentState.pagePosition != position) {
            saveProgress(position)
            _state.update { old -> old.copy(pagePosition = position) }
        }

    }

    private val _state = MutableStateFlow(ManagerState())
    val state = _state.asStateFlow()
    val currentState: ManagerState
        get() = state.value

    suspend fun init(manga: Manga, chapterId: Long) = withDefaultContext {
        val list = chapterDao.getItemsWhereManga(manga.name)

        val chapters =
            if (manga.isAlternativeSort) {
                list.sortedWith(ChapterComparator())
            } else {
                list
            }

        val currentChapterPosition = findChapterPosition(chapters, chapterId)
        val currentChapter = chapters[currentChapterPosition]

        val currentPagePosition = when { // Установка текущей страницы
            currentChapter.progress <= 1 -> 1 // Если не больше 0, то ноль
            else -> currentChapter.progress // Иначе как есть
        }

        _state.update { old ->
            old.copy(
                pagePosition = currentPagePosition,

                chapters = chapters,
                chapterPosition = currentChapterPosition
            ).updatePages() // Получение списка страниц для текущей главы
        }

        staticticPosition = currentPagePosition

        statisticItem = statisticDao.getItem(manga.name)
        statisticItem.lastChapters = 0
        statisticItem.lastPages = 0
        statisticItem.lastDownloadSize = 0
        statisticItem.lastDownloadTime = 0
        statisticDao.update(statisticItem)
    }

    fun updateCurrentChapter(chapter: Chapter) {
        _state.update { old ->
            old.copy(chapters = old.chapters.toMutableList()
                .apply { set(old.chapterPosition, chapter) }
            ).updatePages()
        }
    }

    suspend fun nextPage() {
        updatePagePosition(currentState.pagePosition + 1)
    }

    suspend fun prevPage() {
        updatePagePosition(currentState.pagePosition - 1)
    }

    suspend fun nextChapter() { // переключение на следующую главу
        if (currentState.hasNextChapter()) {
            _state.update { old ->
                old.copy(
                    pagePosition = 1,
                    chapterPosition = old.chapterPosition + 1
                ).updatePages()
            }
            statisticItem.lastChapters++
            statisticItem.allChapters++
            statisticDao.update(statisticItem)
        }
    }

    fun prevChapter() { // переключение на предыдущию главу
        if (currentState.hasPrevChapter()) {
            _state.update { old ->
                old.copy(
                    pagePosition = 1,
                    chapterPosition = old.chapterPosition - 1
                ).updatePages()
            }
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

    private suspend fun saveProgress(pos: Int) { // Сохранение позиции текущей главы
        var p = pos // скопировать позицию
        val chapter = currentState.currentChapter

        when {
            pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
            pos == currentState.pages.size - 2 -> { // если текущая позиция последняя
                log("pos is $pos")
                log("size is ${currentState.pages.size}")
                p = currentState.pages.size - 2
                // Сделать главу прочитанной
                chapter.isRead = true
            }
            pos >= currentState.pages.size - 2 -> return // Если больше максимального значения, ничего не делать
        }
        // Обновить позицию
        chapter.progress = p
        chapterDao.update(chapter)

        // сохрание статистики
        if (pos > staticticPosition) {
            val diff = pos - staticticPosition
            statisticItem.lastPages += diff
            statisticItem.allPages += diff
            staticticPosition = pos
            statisticDao.update(statisticItem)
        }
    }
}

internal data class ManagerState(
    val pages: List<Page> = emptyList(), // Список страниц
    val pagePosition: Int = -1,

    val chapters: List<Chapter> = emptyList(), // Список глав
    val chapterPosition: Int = -1, // Позиция текущей глава
) {
    val uiChapterPosition: Int // Позиция текущей главы для ui
        get() = chapterPosition + 1

    val currentChapter: Chapter // Текущая глава
        get() =
            if (chapterPosition > -1 && chapters.isNotEmpty()) {
                chapters[chapterPosition]
            } else {
                Chapter()
            }
}

// проверки наличия следующей и предыдущей главы
internal fun ManagerState.hasNextChapter() = chapterPosition < chapters.size - 1
internal fun ManagerState.hasPrevChapter() = chapterPosition > 0

// подготовка и обновления списка страниц
internal fun ManagerState.updatePages(): ManagerState {
    val pages = mutableListOf<Page>()

    if (hasPrevChapter())  // Если есть главы до этой
        pages.add(Page.Prev) // Добавить в конец указатель наличия предыдущей главы
    else  // если нет
        pages.add(Page.NonePrev) // Добавить в конец указатель отсутствия предыдущей главы

    pages.addAll(
        currentChapter.pages
            .map<String, Page> { Page.Current(it, currentChapter) })

    if (hasNextChapter())  // Если есть главы после этой
        pages.add(Page.Next) // Добавить в конец указатель наличия следующей главы
    else // если нет
        pages.add(Page.NoneNext) // Добавить в конец указатель отсутствия следующей главы

    return copy(pages = pages)
}

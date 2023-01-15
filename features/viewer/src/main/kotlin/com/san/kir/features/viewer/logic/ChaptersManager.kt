package com.san.kir.features.viewer.logic

import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.features.viewer.utils.Page
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

// класс для управления страницами и главами
internal class ChaptersManager @Inject constructor(
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
    private val siteCatalogManager: SiteCatalogsManager,
) {
    // Вспомогательная переменная для расчета количества прочитанных страниц за сессию
    private var staticticPosition = 0
    var statisticItem = Statistic()
        private set

    fun updateStatisticData(downloadSize: Long, downloadTime: Long) {
        if (downloadSize > 100L) {
            statisticItem = statisticItem.copy(
                lastDownloadSize = statisticItem.lastDownloadSize + downloadSize,
                downloadSize = statisticItem.downloadSize + downloadSize,
                lastDownloadTime = statisticItem.lastDownloadTime + downloadTime,
                downloadTime = statisticItem.downloadTime + downloadTime,
            )
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
    private val currentState: ManagerState
        get() = state.value

    suspend fun init(manga: Manga, chapterId: Long) = withDefaultContext {
        val list = chapterDao.itemsByMangaId(manga.id)

        val chapters =
            if (manga.isAlternativeSort) {
                list.sortedWith(ChapterComparator())
            } else {
                list
            }

        val currentChapterPosition = findChapterPosition(chapters, chapterId)
        val currentChapter = chapters[currentChapterPosition]
        val currentPagePosition = maxOf(1, currentChapter.progress)

        staticticPosition = currentPagePosition

        val statisticId = statisticDao.idByMangaId(manga.id)

        statisticItem = if (statisticId == null)
            statisticDao.itemById(
                statisticDao.insert(Statistic(mangaId = manga.id)).first()
            )
        else statisticDao.itemById(statisticId)

        statisticItem = statisticItem.copy(
            lastChapters = 0,
            lastPages = 0,
            lastDownloadSize = 0,
            lastDownloadTime = 0,
        )
        statisticDao.update(statisticItem)

        _state.update { old ->
            old.copy(
                pagePosition = currentPagePosition,
                chapterPosition = currentChapterPosition,
                chapters = chapters.toMutableList().apply {
                    // Если страницы пустые, то обновляем их
                    if (currentChapter.pages.isEmpty())
                        set(currentChapterPosition, currentChapter.withUpdatedPages())
                }
            ).preparePages()
        }
    }

    suspend fun updatePagesForCurrentChapter(chapter: Chapter = currentState.currentChapter) {
        updateCurrentChapter(chapter.withUpdatedPages())
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
                ).preparePages()
            }
            statisticItem = statisticItem.copy(
                lastChapters = statisticItem.lastChapters + 1,
                allChapters = statisticItem.allChapters + 1,
            )
            statisticDao.update(statisticItem)
        }
    }

    fun prevChapter() { // переключение на предыдущию главу
        if (currentState.hasPrevChapter()) {
            _state.update { old ->
                old.copy(
                    pagePosition = 1,
                    chapterPosition = old.chapterPosition - 1
                ).preparePages()
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

    private fun updateCurrentChapter(chapter: Chapter) {
        _state.update { old ->
            old.copy(
                chapters = old.chapters
                    .toMutableList()
                    .apply { set(old.chapterPosition, chapter) }
            ).preparePages()
        }
    }

    private suspend fun saveProgress(pos: Int) { // Сохранение позиции текущей главы
        var p = pos // скопировать позицию
        var chapter = currentState.currentChapter

        when {
            pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
            pos == currentState.pages.size - 2 -> { // если текущая позиция последняя
                Timber.v("pos is $pos")
                Timber.v("size is ${currentState.pages.size}")
                p = currentState.pages.size - 2
                // Сделать главу прочитанной
                chapter = chapter.copy(isRead = true)
            }

            pos >= currentState.pages.size - 2 -> return // Если больше максимального значения, ничего не делать
        }
        // Обновить позицию
        chapterDao.update(chapter.copy(progress = p))

        // сохрание статистики
        if (pos > staticticPosition) {
            val diff = pos - staticticPosition

            statisticItem = statisticItem.copy(
                lastPages = statisticItem.lastPages + diff,
                allPages = statisticItem.allPages + diff,
            )
            staticticPosition = pos
            statisticDao.update(statisticItem)
        }
    }

    private suspend fun Chapter.withUpdatedPages(): Chapter {
        val chapter = copy(pages = siteCatalogManager.pages(this))
        chapterDao.update(chapter)
        return chapter
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
            } else Chapter()

    override fun toString(): String {
        return buildString {
            appendLine("ManagerState")
            appendLine("\tpages -> ${pages.size}")
            appendLine("\tpage -> ${pagePosition}")
            appendLine("\tchapters -> ${chapters.size}")
            appendLine("\tchapter -> ${chapterPosition}")
        }
    }
}

// проверки наличия следующей и предыдущей главы
internal fun ManagerState.hasNextChapter() = chapterPosition < chapters.size - 1
internal fun ManagerState.hasPrevChapter() = chapterPosition > 0

// подготовка и обновления списка страниц
internal fun ManagerState.preparePages(): ManagerState {
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

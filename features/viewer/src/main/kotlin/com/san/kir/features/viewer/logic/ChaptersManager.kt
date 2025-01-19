package com.san.kir.features.viewer.logic

import com.san.kir.core.internet.AuthorizationException
import com.san.kir.core.internet.PageNotFoundException
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.set
import com.san.kir.data.db.main.repo.ChapterRepository
import com.san.kir.data.db.main.repo.StatisticsRepository
import com.san.kir.data.models.main.Chapter
import com.san.kir.data.models.main.Manga
import com.san.kir.data.models.main.Statistic
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.features.viewer.utils.Page
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

// класс для управления страницами и главами
internal class ChaptersManager(
    private val chapterRepository: ChapterRepository,
    private val statisticsRepository: StatisticsRepository,
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
            _state.update { old -> old.copy(pagePosition = position) }
            saveProgress(position)
        }
    }

    private val _state = MutableStateFlow(ManagerState())
    val state = _state.asStateFlow()

    private val currentState: ManagerState get() = state.value

    suspend fun init(manga: Manga, chapterId: Long) = withDefaultContext {

        val list = chapterRepository.allItems(manga.id)

        val chapters = if (manga.isAlternativeSort) list.sortedWith(ChapterComparator()) else list

        val currentChapterPosition = findChapterPosition(chapters, chapterId)
        val currentChapter = chapters[currentChapterPosition]
        val currentPagePosition = maxOf(1, currentChapter.progress)

        staticticPosition = currentPagePosition

        _state.update { old ->
            old.copy(
                color = manga.color,
                pagePosition = currentPagePosition,
                chapterPosition = currentChapterPosition,
                chapters = chapters,
            )
        }

        val statisticId = statisticsRepository.idByMangaId(manga.id)

        statisticItem =
            if (statisticId == null)
                statisticsRepository.itemById(statisticsRepository.save(Statistic(mangaId = manga.id)).first())!!
            else
                statisticsRepository.itemById(statisticId)!!

        statisticItem = statisticItem.copy(
            lastChapters = 0,
            lastPages = 0,
            lastDownloadSize = 0,
            lastDownloadTime = 0,
        )
        statisticsRepository.save(statisticItem)

        _state.update { old ->
            old.copy(
                chapters = chapters.updatePages(currentChapterPosition),
            ).preparePages()
        }
    }

    suspend fun updatePagesForCurrentChapter(chapter: Chapter = currentState.currentChapter) {
        updateCurrentChapter(chapter.withUpdatedPages())
    }

    suspend fun nextPage() = updatePagePosition(currentState.pagePosition + 1)
    suspend fun prevPage() = updatePagePosition(currentState.pagePosition - 1)

    suspend fun nextChapter() { // переключение на следующую главу
        if (currentState.hasNextChapter().not()) return
        _state.update { old ->
            old.copy(pagePosition = 1, chapterPosition = old.chapterPosition + 1, pages = emptyList())
        }

        _state.update { old -> old.copy(chapters = old.chapters.updatePages(old.chapterPosition)).preparePages() }
        statisticItem = statisticItem.copy(
            lastChapters = statisticItem.lastChapters + 1,
            allChapters = statisticItem.allChapters + 1,
        )
        statisticsRepository.save(statisticItem)
    }

    fun prevChapter() { // переключение на предыдущию главу
        if (currentState.hasPrevChapter()) {
            _state.update { old ->
                old.copy(pagePosition = 1, chapterPosition = old.chapterPosition - 1).preparePages()
            }
        }
    }

    private fun findChapterPosition(chapters: List<Chapter>, chapterId: Long): Int {
        return (0..chapters.lastIndex).firstOrNull { chapters[it].id == chapterId } ?: 0
    }

    private fun updateCurrentChapter(chapter: Chapter) {
        _state.update { old -> old.copy(chapters = old.chapters.set(old.chapterPosition, chapter)).preparePages() }
    }

    private suspend fun saveProgress(pos: Int) { // Сохранение позиции текущей главы
        var p = pos // скопировать позицию
        var chapter = currentState.currentChapter

        when {
            pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
            pos == currentState.pages.size - 2 -> { // если текущая позиция последняя
                Timber.v("pos is $pos \nsize is ${currentState.pages.size}")
                p = currentState.pages.size - 2
                // Сделать главу прочитанной
                chapter = chapter.copy(isRead = true)
            }

            pos >= currentState.pages.size - 2 -> return // Если больше максимального значения, ничего не делать
        }
        // Обновить позицию
        chapterRepository.save(chapter.copy(progress = p))

        // сохрание статистики
        if (pos > staticticPosition) {
            val diff = pos - staticticPosition

            statisticItem = statisticItem.copy(
                lastPages = statisticItem.lastPages + diff,
                allPages = statisticItem.allPages + diff,
            )
            staticticPosition = pos
            statisticsRepository.save(statisticItem)
        }
    }

    // Если страницы пустые, то обновляем их
    private suspend fun Chapter.withUpdatedPages(): Chapter {
        val pages = runCatching { siteCatalogManager.pages(this) }
            .onFailure { ex ->
                Timber.v(ex)
                val errorState = when (ex) {
                    is AuthorizationException -> ErrorState.AuthError(siteCatalogManager.catalog(link).name)
                    is PageNotFoundException -> ErrorState.NotFoundError
                    else -> ErrorState.BaseError(ex.localizedMessage ?: "Unknown error")
                }
                _state.update { it.copy(error = errorState) }
            }
            .getOrDefault(emptyList())
        val chapter = copy(pages = pages)
        chapterRepository.save(chapter)
        return chapter
    }

    private suspend fun List<Chapter>.updatePages(chapterPosition: Int): List<Chapter> {
        val chapter = get(chapterPosition)
        return if (chapter.pages.all { it.isBlank() }.not()) this
        else set(chapterPosition, get(chapterPosition).withUpdatedPages())
    }
}

internal data class ManagerState(
    val pages: List<Page> = emptyList(), // Список страниц
    val pagePosition: Int = -1,

    val chapters: List<Chapter> = emptyList(), // Список глав
    val chapterPosition: Int = -1, // Позиция текущей глава

    val error: ErrorState = ErrorState.None,

    val color: Int = 0,
) {
    val uiChapterPosition: Int // Позиция текущей главы для ui
        get() = chapterPosition + 1

    val currentChapter: Chapter // Текущая глава
        get() = if (chapterPosition in chapters.indices) chapters[chapterPosition] else Chapter()

    override fun toString(): String {
        return buildString {
            appendLine("ManagerState")
            appendLine("\tpages -> ${pages.size}")
            appendLine("\tpage -> $pagePosition")
            appendLine("\tchapters -> ${chapters.size}")
            appendLine("\tchapter -> $chapterPosition")
        }
    }
}

internal sealed interface ErrorState {
    data object None : ErrorState
    data object NotFoundError : ErrorState
    data class BaseError(val text: String) : ErrorState
    data class AuthError(val catalogName: String) : ErrorState
}

// проверки наличия следующей и предыдущей главы
internal fun ManagerState.hasNextChapter() = chapterPosition < chapters.size - 1
internal fun ManagerState.hasPrevChapter() = chapterPosition > 0

// подготовка и обновления списка страниц
internal fun ManagerState.preparePages(): ManagerState {
    val pages = mutableListOf<Page>()

    if (hasPrevChapter()) pages.add(Page.Prev) // Добавить в конец указатель наличия предыдущей главы
    else pages.add(Page.NonePrev) // Добавить в конец указатель отсутствия предыдущей главы

    pages.addAll(currentChapter.pages.map<String, Page> { Page.Current(it, currentChapter) })

    if (hasNextChapter()) pages.add(Page.Next) // Добавить в конец указатель наличия следующей главы
    else pages.add(Page.NoneNext) // Добавить в конец указатель отсутствия следующей главы

    return copy(pages = pages)
}

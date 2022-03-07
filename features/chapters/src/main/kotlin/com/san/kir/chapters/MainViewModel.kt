package com.san.kir.chapters

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.download.DownloadService
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.delChapters
import com.san.kir.core.utils.log
import com.san.kir.core.utils.toast
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.action
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.data.store.ChaptersStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    private val chapterStore: ChaptersStore,
    private val context: Application,
    private val manager: SiteCatalogsManager,
) : ViewModel() {

    val selection = SelectionControl()

    private var oneTimeFlag = MutableStateFlow(true)

    // Настройка отображения заголовков вкладок
    val hasTitle = chapterStore.data.map { it.isTitle }

    // Используется такая инициация вместо AssistedInject
    private val mangaUnic = MutableStateFlow("")
    fun setMangaUnic(manga: String) {
        mangaUnic.update { manga }
    }

    // фильтры для списка глав
    private val _filter = MutableStateFlow(ChapterFilter.ALL_READ_ASC)
    val filter = _filter.asStateFlow()
    fun updateFilter(filter: ChapterFilter) {
        _filter.value = filter
    }

    // Получение Манги из бд
    val manga = mangaUnic
        .filterNot { it.isEmpty() }
        .flatMapLatest {
            log("manga is $it")
            mangaDao.loadItemByName(it)
        }
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, Manga())

    // Получение всех глав для манги
    val chapters = manga
        .flatMapLatest { item -> chapterDao.loadItemsWhereManga(item.name) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Главы с применеными фильтрами и сортировкой для отображения списка
    val prepareChapters =
        combine(chapters, filter, manga) { list, filter, manga ->
            list.applyFilter(filter, manga)
        }
            .distinctUntilChanged()
            .catch { t -> throw t }
            .onEach { list -> selection.updateSelectedSize(list.count()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Первая не прочитанная глава, для кнопки продолжить
    val firstNotReadChapter = chapters.combine(manga) { list, manga ->
        val newList = if (manga.isAlternativeSort) {
            try {
                list.sortedWith(ChapterComparator())
            } catch (e: Exception) {
                list
            }
        } else {
            list
        }
        newList.firstOrNull { chapter -> !chapter.isRead }
    }

    init {
        // инициация фильтра
        combine(oneTimeFlag, manga, chapterStore.data) { flag, manga, store ->
            // Производится единожды, как и инкремента использования манги
            if (flag) {
                manga.populate += 1
                mangaDao.update(manga)
                oneTimeFlag.value = false

                // взависимости от настройки используется общий или индивидуальный фильтр
                _filter.update {
                    if (store.isIndividual) {
                        manga.chapterFilter
                    } else {
                        ChapterFilter.valueOf(store.filterStatus)
                    }
                }
            }
        }.launchIn(viewModelScope)

        // Прослушивание фильтра для сохранения
        combine(chapterStore.data, filter) { store, f ->
            if (store.isIndividual) {
                manga.value.apply {
                    chapterFilter = f
                    mangaDao.update(this)
                }
            } else {
                chapterStore.setFilter(f.name)
            }
        }.launchIn(viewModelScope)
    }

    // Удаление файлов для выделенных глав
    fun deleteSelectedItems() = viewModelScope.launch {
        var count = 0
        selection.with(prepareChapters.value).forEachIndexed { _, (b, chapter) ->
            if (b && chapter.action == ChapterStatus.DELETE) {
                delChapters(chapter.path)
                count++
            }
        }
        withMainContext {
            if (count == 0) {
                context.toast(R.string.list_chapters_selection_del_error)
            } else {
                context.toast(R.string.list_chapters_selection_del_ok)
            }
        }
        selection.clear()
    }

    // Загрузка выделенных глав
    fun downloadSelectedItems() = viewModelScope.launch {
        selection.with(prepareChapters.value).forEach { (b, chapter) ->
            if (b && chapter.action == ChapterStatus.DOWNLOADABLE)
                DownloadService.start(context, chapter)
        }
        selection.clear()
    }

    // Удаление выделенных глав из БД
    fun fullDeleteSelectedItems() = viewModelScope.launch {
        chapterDao.delete(
            selection.with(prepareChapters.value)
                .filter { (b, _) -> b }
                .map { (_, ch) -> ch }
        )
    }

    // Смена статуса чтения для выделенных глав
    fun setReadStatus(state: Boolean) = viewModelScope.launch {
        chapterDao.update(
            selection.with(prepareChapters.value)
                .filter { it.first }
                .map { (_, chapter) ->
                    chapter.isRead = state
                    chapter
                }
        )

        selection.clear()
    }

    // Обновление страниц для выделенных глав
    fun updatePagesForSelectedItems() = viewModelScope.launch {
        selection.with(prepareChapters.value).forEachIndexed { _, (b, chapter) ->
            if (b) {
                chapter.pages = manager.pages(chapter)
                chapterDao.update(chapter)
            }
        }
        selection.clear()
    }

    // подготовка списка глав с использованием фильтров и сортировки
    private fun List<Chapter>.applyFilter(filter: ChapterFilter, manga: Manga): List<Chapter> {
        var list = this
        if (manga.isAlternativeSort) {
            list = list.sortedWith(ChapterComparator())
        }
        return when (filter) {
            ChapterFilter.ALL_READ_ASC -> list
            ChapterFilter.NOT_READ_ASC -> list.filter { !it.isRead }
            ChapterFilter.IS_READ_ASC -> list.filter { it.isRead }
            ChapterFilter.ALL_READ_DESC -> list.reversed()
            ChapterFilter.NOT_READ_DESC -> list.filter { !it.isRead }.reversed()
            ChapterFilter.IS_READ_DESC -> list.filter { it.isRead }.reversed()
        }
    }
}


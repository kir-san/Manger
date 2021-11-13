package com.san.kir.manger.ui.application_navigation.library.chapters

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.data.datastore.ChaptersRepository
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.di.MainDispatcher
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.utils.ChapterComparator
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.extensions.delChapters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaptersViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    private val chapterStore: ChaptersRepository,
    private val context: Application,
    private val manager: SiteCatalogsManager,
    @DefaultDispatcher private val default: CoroutineDispatcher,
    @MainDispatcher private val main: CoroutineDispatcher,
) : ViewModel() {
    private var oneTimeFlag by mutableStateOf(true)
    var manga by mutableStateOf(Manga())
        private set

    // for selection mode
    var selectedItems by mutableStateOf<List<Boolean>>(listOf())
        private set

    var selectionMode by mutableStateOf(false)
        private set

    // chapters
    var chapters by mutableStateOf<List<Chapter>>(listOf())
        private set

    var prepareChapters by mutableStateOf<List<Chapter>>(listOf())
        private set

    // фильтры для списка глав
    var filter by mutableStateOf(ChapterFilter.ALL_READ_ASC)

    init {
        // инициация манги
        combine(
            mangaDao.loadItem(mangaUnic).filterNotNull(),
            snapshotFlow { oneTimeFlag }
        ) { m, flag ->
            if (flag) {
                m.populate += 1
                withContext(main) {
                    oneTimeFlag = false
                    manga = m
                }
                mangaDao.update(m)
            }
            m
        }
            .flatMapLatest { manga ->
                withContext(main) { this@ChaptersViewModel.manga = manga }
                chapterDao.loadItemsWhereManga(manga.unic)
            }
            .onEach { withContext(main) { chapters = it } }
            // подготовка списка глав с использованием фильтров и сортировки
            .combine(snapshotFlow { filter }) { list, f -> list to f }
            .map { (l, filter) ->
                var list = l
                if (manga.isAlternativeSort) {
                    list = list.sortedWith(ChapterComparator())
                }
                when (filter) {
                    ChapterFilter.ALL_READ_ASC -> list
                    ChapterFilter.NOT_READ_ASC -> list.filter { !it.isRead }
                    ChapterFilter.IS_READ_ASC -> list.filter { it.isRead }
                    ChapterFilter.ALL_READ_DESC -> list.reversed()
                    ChapterFilter.NOT_READ_DESC -> list.filter { !it.isRead }.reversed()
                    ChapterFilter.IS_READ_DESC -> list.filter { it.isRead }.reversed()
                }
            }
            .distinctUntilChanged()
            .catch { t -> throw t }
            .onEach { withContext(main) { prepareChapters = it } }
            // обновление размера списка выделеных элементов
            .map { it.count() }
            .onEach { count ->
                if (selectedItems.count() != count) {
                    withContext(main) {
                        selectedItems = List(count) { false }
                    }
                }
            }
            .launchIn(viewModelScope)

        // инициация фильтра
        combine(chapterStore.data, snapshotFlow { manga }) { store, manga ->
            if (store.isIndividual) {
                manga.chapterFilter
            } else {
                ChapterFilter.valueOf(store.filterStatus)
            }
        }
            .catch { t -> throw t }
            .distinctUntilChanged()
            .onEach { withContext(main) { filter = it } }
            .launchIn(viewModelScope)

        // Прослушивание фильтра для сохранения
        combine(chapterStore.data, snapshotFlow { filter }) { store, filter ->
            if (store.isIndividual) {
                manga = manga.apply {
                    chapterFilter = filter
                }
                mangaDao.update(manga)
            } else {
                chapterStore.setFilter(filter.name)
            }
        }.launchIn(viewModelScope)

        // активация и дезактивация режима выделения
        snapshotFlow { selectionMode to selectedItems }
            .map { (mode, list) -> mode to list.count { it } }
            .onEach { (mode, count) ->
                if (count > 0 && mode.not()) {
                    withContext(main) {
                        selectionMode = true
                    }
                } else if (count <= 0 && mode) {
                    withContext(main) {
                        selectionMode = false
                    }

                }
            }.launchIn(viewModelScope)
    }

    val isTitle = chapterStore.data.map { it.isTitle }

    fun onSelectItem(index: Int) = viewModelScope.launch {
        selectedItems = selectedItems.toMutableList().apply { set(index, get(index).not()) }
    }

    fun selectAllItems() = viewModelScope.launch {
        selectedItems = List(selectedItems.count()) { true }
    }

    fun removeSelection() = viewModelScope.launch {
        selectedItems = List(selectedItems.count()) { false }
    }

    fun selectBelowItems() = viewModelScope.launch {
        val countSelectedItems = selectedItems.count { it }
        if (countSelectedItems == 1) {
            val start = selectedItems.indexOf(true)
            selectedItems = List(start) { false } + List(selectedItems.count() - start) { true }
        }
    }

    fun selectAboveItems() = viewModelScope.launch {
        val countSelectedItems = selectedItems.count { it }
        if (countSelectedItems == 1) {
            val start = selectedItems.indexOf(true)
            selectedItems =
                List(start + 1) { true } + List(selectedItems.count() - start - 1) { false }
        }
    }

    fun deleteSelectedItems() = viewModelScope.launch {
        var count = 0
        selectedItems.zip(prepareChapters).forEachIndexed { i, (b, chapter) ->
            if (b && chapter.action == ChapterStatus.DELETE) {
                delChapters(chapter)
                count++
                selectedItems = selectedItems.toMutableList().apply { set(i, false) }
            }
        }
        withContext(default) {
            if (count == 0) {
                context.toast(R.string.list_chapters_selection_del_error)
            } else {
                context.toast(R.string.list_chapters_selection_del_ok)
            }
        }
        removeSelection()
    }

    fun downloadSelectedItems() = viewModelScope.launch {
        selectedItems.zip(prepareChapters).forEach { (b, chapter) ->
            if (b && chapter.action == ChapterStatus.DOWNLOADABLE)
                DownloadService.start(context, chapter)
        }
        removeSelection()
    }

    fun fullDeleteSelectedItems() = viewModelScope.launch {
        selectedItems
            .zip(prepareChapters)
            .filter { (b, _) -> b }
            .map { (_, ch) -> ch }
            .forEach { chapterDao.delete(it) }
    }

    fun setReadStatus(state: Boolean) = viewModelScope.launch {
        selectedItems.zip(prepareChapters).forEachIndexed { i, (b, chapter) ->
            if (b) {
                chapter.isRead = state
                chapterDao.update(chapter)
            }
        }
        removeSelection()
    }

    fun updatePagesForSelectedItems() = viewModelScope.launch {
        selectedItems.zip(prepareChapters).forEachIndexed { i, (b, chapter) ->
            if (b) {
                chapter.pages = manager.pages(chapter)
                chapterDao.update(chapter)
            }
        }
        removeSelection()
    }

    suspend fun getFirstNotReadChapters(manga: Manga): Chapter? = withContext(default) {
        var list = chapterDao.getItemsWhereManga(manga.unic)
        list = if (manga.isAlternativeSort)
            try {
                list.sortedWith(ChapterComparator())
            } catch (e: Exception) {
                list
            }
        else list

        list.firstOrNull { !it.isRead }
    }

    @AssistedFactory
    interface Factory {
        fun create(mangaUnic: String): ChaptersViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            mangaUnic: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(mangaUnic) as T
            }
        }
    }
}

@Composable
fun chaptersViewModel(mangaUnic: String): ChaptersViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).chaptersViewModelFactory()

    return viewModel(factory = ChaptersViewModel.provideFactory(factory, mangaUnic))
}

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
import com.san.kir.manger.utils.extensions.log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaptersViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    private val chapterStore: ChaptersRepository,
    private val context: Application,
    private val manager: SiteCatalogsManager,
) : ViewModel() {
    private val _oneTimeFlag = MutableStateFlow(true)
    private val _manga = MutableStateFlow(Manga())
    val manga = _manga.asStateFlow()

    val isTitle = chapterStore.data.map { it.isTitle }

    var selectionMode by mutableStateOf(false)

    // фильтры для списка глав
    private val _filter = MutableStateFlow(ChapterFilter.ALL_READ_ASC)
    val filter = _filter.asStateFlow()
    fun changeFilter(action: (ChapterFilter) -> ChapterFilter) {
        _filter.value = action(_filter.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val chapters = _manga.flatMapLatest { manga -> chapterDao.loadItems(manga.unic) }

    var prepareChapters by mutableStateOf<List<Chapter>>(listOf())
        private set

    // for selection mode
    var selectedItems by mutableStateOf<List<Boolean>>(listOf())
        private set

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
        withContext(Dispatchers.Main) {
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

    init {
        // инициация манги
        viewModelScope.launch(Dispatchers.Default) {
            log("init manga $mangaUnic")
            combine(
                _oneTimeFlag,
                mangaDao.loadItem(mangaUnic).filterNotNull(),
            ) { flag, manga ->
                if (flag) {
                    manga.populate += 1
                    mangaDao.update(manga)
                    _oneTimeFlag.value = false
                }
                manga
            }.collect {
                _manga.value = it
            }
        }
        // инициация фильтра
        viewModelScope.launch(Dispatchers.Default) {
            combine(chapterStore.data, _manga) { store, manga ->
                if (store.isIndividual) {
                    manga.chapterFilter
                } else {
                    ChapterFilter.valueOf(store.filterStatus)
                }
            }
                .catch { t -> throw t }
                .collect {
                    _filter.value = it
                }
        }
        // Прослушивание фильтра для сохранения
        viewModelScope.launch(Dispatchers.Default) {
            combine(chapterStore.data, filter) { store, filter ->
                if (store.isIndividual) {
                    _manga.value = _manga.value.apply {
                        chapterFilter = filter
                        mangaDao.update(this)
                    }
                } else {
                    chapterStore.setFilter(filter.name)
                }
            }.collect()
        }
        // активация и дезактивация режима выделения
        snapshotFlow { selectionMode to selectedItems }
            .map { (mode, list) -> mode to list.count { it } }
            .onEach { (mode, count) ->
                if (count > 0 && mode.not()) {
                    selectionMode = true
                } else if (count <= 0 && mode) {
                    selectionMode = false
                }
            }.launchIn(viewModelScope)

        // подготовка списка глав с использованием фильтров и сортировки
        viewModelScope.launch(Dispatchers.Default) {
            combine(chapters, _manga, _filter) { chapters, manga, filter ->
                var list = chapters
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
                .onEach {
                    prepareChapters = it
                }
                // обновление размера списка выделеных элементов
                .map { it.count() }
                .onEach { count ->
                    if (selectedItems.count() != count) {
                        withContext(Dispatchers.Main) {
                            log("set selected items")
                            selectedItems = List(count) { false }
                        }
                    }
                }
                .collect()
        }
    }

    suspend fun getFirstNotReadChapters(manga: Manga): Chapter? = withContext(Dispatchers.Default) {
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

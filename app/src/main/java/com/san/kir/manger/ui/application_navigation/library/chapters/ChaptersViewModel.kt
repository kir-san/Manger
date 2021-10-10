package com.san.kir.manger.ui.application_navigation.library.chapters

import android.app.Application
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()

    // фильтры для списка глав
    private val _filter = MutableStateFlow(ChapterFilter.ALL_READ_ASC)
    val filter = _filter.asStateFlow()
    fun changeFilter(action: (ChapterFilter) -> ChapterFilter) {
        _filter.value = action(_filter.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val chapters = _manga.flatMapLatest { manga -> chapterDao.loadItems(manga.unic) }

    private val _prepareChapters = MutableStateFlow(listOf<Chapter>())
    val prepareChapters = _prepareChapters.asStateFlow()

    // for selection mode
    private val _selectedItems = MutableStateFlow(listOf<Boolean>())
    val selectedItems = _selectedItems.asStateFlow()
    fun onSelectItem(index: Int) = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.update { old ->
            old.toMutableList().apply { set(index, get(index).not()) }
        }
    }

    fun selectAllItems() = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.update { old -> List(old.count()) { true } }
    }

    fun removeSelection() = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.update { old -> List(old.count()) { false } }
    }

    fun selectBelowItems() = viewModelScope.launch(Dispatchers.Default) {
        val countSelectedItems = selectedItems.value.count { it }
        if (countSelectedItems == 1) {
            val start = selectedItems.value.indexOf(true)
            _selectedItems.update { old ->
                List(start) { false } + List(old.count() - start) { true }
            }
        }
    }

    fun selectAboveItems() = viewModelScope.launch(Dispatchers.Default) {
        val countSelectedItems = selectedItems.value.count { it }
        if (countSelectedItems == 1) {
            val start = selectedItems.value.indexOf(true)
            _selectedItems.update { old ->
                List(start + 1) { true } + List(old.count() - start - 1) { false }
            }
        }
    }

    fun deleteSelectedItems() = viewModelScope.launch(Dispatchers.Default) {
        var count = 0
        _selectedItems.value.zip(_prepareChapters.value).forEachIndexed { i, (b, chapter) ->
            if (b && chapter.action == ChapterStatus.DELETE) {
                delChapters(chapter)
                count++
                _selectedItems.update { old -> old.toMutableList().apply { set(i, false) } }
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

    fun downloadSelectedItems() = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.value.zip(_prepareChapters.value).forEach { (b, chapter) ->
            if (b && chapter.action == ChapterStatus.DOWNLOADABLE)
                DownloadService.start(context, chapter)
        }
        removeSelection()
    }

    fun fullDeleteSelectedItems() = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.value
            .zip(_prepareChapters.value)
            .filter { (b, _) -> b }
            .map { (_, ch) -> ch }
            .forEach { chapterDao.delete(it) }
    }

    fun setReadStatus(state: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.value.zip(_prepareChapters.value).forEachIndexed { i, (b, chapter) ->
            if (b) {
                chapter.isRead = state
                chapterDao.update(chapter)
            }
        }
        removeSelection()
    }

    fun updatePagesForSelectedItems() = viewModelScope.launch(Dispatchers.Default) {
        _selectedItems.value.zip(_prepareChapters.value).forEachIndexed { i, (b, chapter) ->
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
        // обновление размера списка выделеных элементов
        viewModelScope.launch(Dispatchers.Default) {
            chapters.map { it.count() }.collect { count ->
                _selectedItems.update { old ->
                    if (old.count() != count) {
                        List(count) { false }
                    } else {
                        old
                    }
                }
            }
        }
        // активация и дезактивация режима выделения
        viewModelScope.launch(Dispatchers.Default) {
            combine(
                _selectedItems.map { array -> array.count { it } },
                _selectionMode
            ) { selectedCount, mode ->
                if (selectedCount > 0 && mode.not()) {
                    _selectionMode.update { true }
                } else if (selectedCount <= 0 && mode) {
                    _selectionMode.update { false }
                }
            }.collect()
        }

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
                .catch { t -> throw t }
                .collect { _prepareChapters.value = it }
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

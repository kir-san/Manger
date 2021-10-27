package com.san.kir.manger.ui.application_navigation.library.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.di.MainDispatcher
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.CategoryWithMangas
import com.san.kir.manger.room.entities.SimpleManga
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.workmanager.MangaDeleteWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val context: Application,
    categoryDao: CategoryDao,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    dataStore: MainRepository,
    @DefaultDispatcher private val default: CoroutineDispatcher,
    @MainDispatcher private val main: CoroutineDispatcher,
) : ViewModel() {
    private val _isAction = MutableStateFlow(false)
    val isAction = _isAction.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty = _isEmpty.asStateFlow()

    private val _currentCategoryWithManga = MutableStateFlow(CategoryWithMangas())
    val currentCategoryWithManga = _currentCategoryWithManga.asStateFlow()

    var selectedManga by mutableStateOf(SelectedManga())
        private set

    val showCategory = dataStore.data.map { it.isShowCatagery }.flowOn(default)

    val categories = categoryDao.loadItems()
        .map { l -> l.map { it.name } }
        .flowOn(default)

    var categoryNames by mutableStateOf(emptyList<String>())
        private set


    val preparedCategories = categoryDao
        .loadItemsAdds()
        .onEmpty { _isEmpty.update { true } }
        .onEach { cats -> withContext(main) { categoryNames = cats.map { it.category.name } } }
        .map { cats ->
            cats.onEach { c ->
                if (c.category.name == CATEGORY_ALL)
                    c.mangas = mangaDao.getSimpleItems()
            }
                .onEach { c ->
                    val list =
                        when (c.category.typeSort) {
                            SortLibraryUtil.add -> c.mangas.sortedBy { it.id }
                            SortLibraryUtil.abc -> c.mangas.sortedBy { it.name }
                            SortLibraryUtil.pop -> c.mangas.sortedBy { it.populate }
                            else -> c.mangas
                        }
                    c.mangas = if (c.category.isReverseSort)
                        list.reversed()
                    else
                        list
                }
        }
        .flowOn(default)

    init {
        viewModelScope.launch(default) {
            WorkManager
                .getInstance(context)
                .getWorkInfosByTagLiveData(MangaDeleteWorker.tag)
                .asFlow()
                .collect { works ->
                    if (works.isNotEmpty()) {
                        _isAction.value = works.all { it.state.isFinished }.not()
                    }
                }
        }
    }

    fun countNotRead(mangaUnic: String) = chapterDao.loadCountItemsWhereManga(mangaUnic)

    fun update(manga: SimpleManga) {
        viewModelScope.launch(default) {
            mangaDao.getItem(manga.unic).apply {
                categories = manga.categories
                mangaDao.update(this)
            }
        }
    }

    fun changeCurrentCategory(newCategoryWithMangas: CategoryWithMangas) {
        viewModelScope.launch(default) {
            _currentCategoryWithManga.update { newCategoryWithMangas }
        }
    }

    fun changeSelectedManga(visible: Boolean, manga: SimpleManga? = null) {
        selectedManga =
            manga?.let { SelectedManga(manga, visible) } ?: selectedManga.copy(visible = visible)
    }
}

data class SelectedManga(
    val manga: SimpleManga = SimpleManga(),
    val visible: Boolean = false,
)

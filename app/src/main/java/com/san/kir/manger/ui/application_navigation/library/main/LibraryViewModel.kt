package com.san.kir.manger.ui.application_navigation.library.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.CategoryWithMangas
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.workmanager.MangaDeleteWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val context: Application,
    categoryDao: CategoryDao,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    dataStore: MainRepository
) : ViewModel() {
    private val _isAction = MutableStateFlow(false)
    val isAction = _isAction.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty = _isEmpty.asStateFlow()

    private val _currentCategoryWithManga = MutableStateFlow(CategoryWithMangas())
    val currentCategoryWithManga = _currentCategoryWithManga.asStateFlow()

    private val _selectedManga = MutableStateFlow(SelectedManga())
    val selectedManga = _selectedManga.asStateFlow()

    val showCategory = dataStore.data.map { it.isShowCatagery }.flowOn(Dispatchers.Default)

    val categories = categoryDao.loadItems()
        .map { l -> l.map { it.name } }
        .flowOn(Dispatchers.Default)

    val categoryNames = categoryDao.loadItems()
        .map { data ->
            data.filter { it.isVisible }
                .map { it.name }
        }
        .flowOn(Dispatchers.Default)


    val preparedCategories = categoryDao
        .loadItemsAdds()
        .onEmpty { _isEmpty.update { true } }
        .map { cats ->
            cats.filter { it.category.isVisible }
                .onEach { c ->
                    if (c.category.name == CATEGORY_ALL)
                        c.mangas = mangaDao.getItems()
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
        .flowOn(Dispatchers.Default)

    init {
        viewModelScope.launch(Dispatchers.Default) {
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

    suspend fun countNotRead(mangaUnic: String) =
        chapterDao.getItemsWhereManga(mangaUnic).filter { !it.isRead }.size

    fun update(manga: Manga) {
        viewModelScope.launch(Dispatchers.Default) {
            mangaDao.update(manga)
        }
    }

    fun changeCurrentCategory(newCategoryWithMangas: CategoryWithMangas) {
        viewModelScope.launch(Dispatchers.Default) {
            _currentCategoryWithManga.update { newCategoryWithMangas }
        }
    }

    fun changeSelectedManga(visible: Boolean, manga: Manga? = null, ) {
        _selectedManga.update {
            manga?.let { SelectedManga(manga, visible) } ?: it.copy(visible = visible)
        }
    }
}

data class SelectedManga(
    val manga: Manga = Manga(),
    val visible: Boolean = false
)

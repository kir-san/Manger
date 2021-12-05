package com.san.kir.manger.ui.application_navigation.library.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.manger.data.room.entities.CategoryWithMangas
import com.san.kir.manger.data.room.entities.SimpleManga
import com.san.kir.manger.foreground_work.workmanager.MangaDeleteWorker
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.core.utils.coroutines.defaultDispatcher
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import com.san.kir.core.utils.coroutines.withMainContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val context: Application,
    categoryDao: com.san.kir.data.db.dao.CategoryDao,
    private val chapterDao: com.san.kir.data.db.dao.ChapterDao,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
    dataStore: MainRepository,
) : ViewModel() {
    private val _isAction = MutableStateFlow(false)
    val isAction = _isAction.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty = _isEmpty.asStateFlow()

    private val _currentCategoryWithManga = MutableStateFlow(CategoryWithMangas())
    val currentCategoryWithManga = _currentCategoryWithManga.asStateFlow()

    var selectedManga by mutableStateOf(SelectedManga())
        private set

    val showCategory = dataStore.data.map { it.isShowCatagery }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    val categories = categoryDao.loadItems()
        .map { l -> l.map { it.name } }
        .flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    var categoryNames by mutableStateOf(emptyList<String>())
        private set


    val preparedCategories = categoryDao
        .loadItemsAdds()
        .onEmpty { _isEmpty.update { true } }
        .onEach { cats ->
            com.san.kir.core.utils.coroutines.withMainContext {
                categoryNames = cats.map { it.category.name }
            }
        }
        .map { cats ->
            cats.onEach { c ->
                if (c.category.name == CATEGORY_ALL)
                    c.mangas = mangaDao.simpleItems()
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
        .flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    init {
        defaultLaunchInVM {
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
        defaultLaunchInVM {
            mangaDao.item(manga.name).apply {
                categories = manga.categories
                mangaDao.update(this)
            }
        }
    }

    fun changeCurrentCategory(newCategoryWithMangas: CategoryWithMangas) {
        defaultLaunchInVM {
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

package com.san.kir.manger.ui.drawer.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.CategoryWithMangas
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.CATEGORY_ALL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    private val dataStore: MainRepository
) : ViewModel() {
    private val isAction = MutableStateFlow(false)

    private val _state = MutableStateFlow(LibraryViewState())
    val state: StateFlow<LibraryViewState>
        get() = _state

    init {
        viewModelScope.launch(Dispatchers.Default) {
            combine(
                categoryDao.loadItemsAdds(),
                isAction,
                dataStore.data
            ) { cats, isa, data ->
                val categories = cats.filter { it.category.isVisible }
                    .onEach {
                        if (it.category.name == CATEGORY_ALL)
                            it.mangas = mangaDao.getItems()
                    }
                LibraryViewState(
                    categories = categories,
                    isAction = isa,
                    isShowCategory = data.isShowCatagery
                )
            }
                .catch { t -> throw t }
                .collect { _state.value = it }
        }
    }

    suspend fun countNotRead(mangaUnic: String) =
        chapterDao.getItems(mangaUnic).filter { !it.isRead }.size

    fun update(manga: Manga) {
        viewModelScope.launch(Dispatchers.Default) {
            mangaDao.update(manga)
        }
    }

    fun setActionState(state: Boolean) {
        isAction.value = state
    }
}

data class LibraryViewState(
    val categories: List<CategoryWithMangas> = emptyList(),
    val isAction: Boolean = false,
    val isShowCategory: Boolean = false
)

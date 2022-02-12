package com.san.kir.manger.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Manga
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OnlyMangaViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    mangaDao: MangaDao,
    categoryDao: CategoryDao,
) : ViewModel() {

    val manga = mangaDao
        .loadItemByName(mangaUnic)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, Manga())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryName = manga
        .flatMapLatest { categoryDao.loadItemById(it.categoryId) }
         .filterNotNull()
        .map { it.name }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    @AssistedFactory
    interface Factory {
        fun create(mangaUnic: String): OnlyMangaViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            mangaUnic: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(mangaUnic) as T
            }
        }
    }
}

@Composable
fun onlyMangaViewModel(mangaUnic: String): OnlyMangaViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).onlyMangaViewModelFactory()

    return viewModel(factory = OnlyMangaViewModel.provideFactory(factory, mangaUnic))
}

package com.san.kir.manger.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Manga
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class OnlyMangaViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val mangaDao: MangaDao,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) : ViewModel() {
    private val _manga = MutableStateFlow(Manga())
    val manga = _manga.asStateFlow()

    init {
        // инициация манги
        viewModelScope.launch(default) {
            mangaDao.loadItem(mangaUnic).filterNotNull().collect { manga ->
                _manga.value = manga
            }
        }
    }

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
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
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

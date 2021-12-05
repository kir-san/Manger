package com.san.kir.manger.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow

class OnlyMangaViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
) : ViewModel() {
    private val _manga = MutableStateFlow(Manga())
    val manga = _manga.asStateFlow()

    init {
        // инициация манги
        defaultLaunchInVM {
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

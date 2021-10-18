package com.san.kir.manger.ui.application_navigation.statistic

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.dao.StatisticDao
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.ui.MainActivity
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

class OnlyStatisticViewModel @AssistedInject constructor(
    @Assisted private val mangaName: String,
    private val statisticDao: StatisticDao,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) : ViewModel() {
    private val _statistic = MutableStateFlow(MangaStatistic())
    val statistic = _statistic.asStateFlow()

    init {
        // инициация манги
        viewModelScope.launch(default) {
            statisticDao.loadItem(mangaName).filterNotNull().collect { statistic ->
                _statistic.value = statistic
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(mangaName: String): OnlyStatisticViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            mangaName: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(mangaName) as T
            }
        }
    }
}

@Composable
fun onlyStatisticViewModel(mangaName: String): OnlyStatisticViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).onlyStatisticViewModelFactory()

    return viewModel(factory = OnlyStatisticViewModel.provideFactory(factory, mangaName))
}

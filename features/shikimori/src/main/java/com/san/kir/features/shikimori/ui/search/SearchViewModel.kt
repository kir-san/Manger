package com.san.kir.features.shikimori.ui.search

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.features.shikimori.logic.repo.ProfileItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class SearchViewModel @Inject internal constructor(
    private val profileItemRepository: ProfileItemRepository,
) : BaseViewModel<SearchEvent, SearchState>() {

    private var job: Job? = null
    private val searchingState = MutableStateFlow<SearchingState>(SearchingState.None)

    override val tempState = searchingState
        .map { search ->
            SearchState(search)
        }

    override val defaultState = SearchState(
        SearchingState.None
    )

    override suspend fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> {
                job?.cancel()
                job = viewModelScope.defaultLaunch {
                    // Добавлена задержка поиска при вводе запроса
                    delay(1.seconds)
                    search(event.text)
                }
            }
        }
    }

    private suspend fun search(text: String) {
        searchingState.update { SearchingState.Load }
        profileItemRepository
            .search(text)
            .onSuccess { items ->
                searchingState.update {
                    if (items.isEmpty()) {
                        SearchingState.None
                    } else {
                        SearchingState.Ok(items)
                    }
                }
            }
            .onFailure {
                searchingState.update { SearchingState.Error }
                Timber.e(it)
            }

    }
}

package com.san.kir.features.shikimori.ui.localItems

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.BackgroundTasks
import com.san.kir.features.shikimori.Helper
import com.san.kir.features.shikimori.HelperImpl
import com.san.kir.features.shikimori.repositories.LibraryItemRepository
import com.san.kir.features.shikimori.repositories.ProfileItemRepository
import com.san.kir.features.shikimori.useCases.BindingHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class LocalItemsViewModel @Inject internal constructor(
    libraryRepository: LibraryItemRepository,
    profileRepository: ProfileItemRepository,
) : BaseViewModel<LocalItemsEvent, LocalItemsState>(),
    Helper<SimplifiedMangaWithChapterCounts> by HelperImpl() {

    private val bindingHelper = BindingHelper(profileRepository)

    init {
        libraryRepository
            .loadItems()
            .mapLatest(bindingHelper.prepareData())
            .onEach(send(true))
            .mapLatest(bindingHelper.checkBinding())
            .onEach(send(false))
            .launchIn(viewModelScope)
    }

    override val tempState = combine(
        unbindedItems, hasAction
    ) { unbind, action ->
        LocalItemsState(action, unbind)
    }

    override val defaultState = LocalItemsState(
        action = BackgroundTasks(),
        unbind = emptyList()
    )

    override fun onEvent(event: LocalItemsEvent) = viewModelScope.launch {
        when (event) {
            else -> {}
        }
    }
}


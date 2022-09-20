package com.san.kir.library.ui.library

import android.app.Application
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.san.kir.background.works.MangaDeleteWorker
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.extend.CategoryWithMangas
import com.san.kir.library.logic.repo.SettingsRepository
import com.san.kir.library.logic.repo.MangaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class LibraryViewModel @Inject internal constructor(
    private val context: Application,
    private val mangaRepository: MangaRepository,
    settingsRepository: SettingsRepository,
) : BaseViewModel<LibraryEvent, LibraryState>() {

    private val selectedMangaState =
        MutableStateFlow<SelectedMangaState>(SelectedMangaState.NonVisible)
    private val currentCategory = MutableStateFlow(CategoryWithMangas())
    private val backgroundState = MutableStateFlow<BackgroundState>(BackgroundState.None)

    init {
        viewModelScope.launch { checkWorks() }
    }

    @Suppress("UNCHECKED_CAST")
    override val tempState = combine(
        selectedMangaState,
        currentCategory,
        mangaRepository.itemsState,
        settingsRepository.main().mapLatest { it.isShowCategory },
        backgroundState,
        ::LibraryState
    )

    override val defaultState = LibraryState(
        selectedManga = SelectedMangaState.NonVisible,
        currentCategory = CategoryWithMangas(),
        items = ItemsState.Load,
        showCategory = false,
        background = BackgroundState.None
    )

    override suspend fun onEvent(event: LibraryEvent) {
        when (event) {
            LibraryEvent.NonSelect -> deSelectManga()

            is LibraryEvent.SelectManga ->
                selectedMangaState.update { SelectedMangaState.Visible(event.item) }

            is LibraryEvent.SetCurrentCategory -> currentCategory.update { event.item }

            is LibraryEvent.ChangeCategory -> {
                deSelectManga()

                val selectedManga =
                    (selectedMangaState.value as? SelectedMangaState.Visible) ?: return
                mangaRepository.changeCategory(selectedManga.item.id, event.categoryId)
            }

            is LibraryEvent.DeleteManga -> {
                deSelectManga()
                MangaDeleteWorker.addTask(context, event.mangaId, event.withFiles)
            }
        }
    }

    private fun deSelectManga() = selectedMangaState.update { SelectedMangaState.NonVisible }

    private suspend fun checkWorks() {
        WorkManager
            .getInstance(context)
            .getWorkInfosByTagLiveData(MangaDeleteWorker.tag)
            .asFlow()
            .filter { it.isNotEmpty() }
            .mapLatest { works -> works.all { it.state.isFinished } }
            .collectLatest { noWork ->
                if (noWork) {
                    backgroundState.update { BackgroundState.None }
                } else {
                    backgroundState.update { BackgroundState.Work }
                }
            }
    }
}

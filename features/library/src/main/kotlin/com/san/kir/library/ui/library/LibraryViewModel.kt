package com.san.kir.library.ui.library

import com.san.kir.background.logic.UpdateMangaManager
import com.san.kir.background.logic.di.updateMangaManager
import com.san.kir.background.works.AppUpdateWorker
import com.san.kir.background.works.MangaDeleteWorker
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.categoryAll
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ViewModel
import com.san.kir.data.categoryRepository
import com.san.kir.data.db.main.repo.CategoryRepository
import com.san.kir.data.db.main.repo.MangaRepository
import com.san.kir.data.db.main.repo.SettingsRepository
import com.san.kir.data.mangaRepository
import com.san.kir.data.models.main.Category
import com.san.kir.data.models.main.CategoryWithMangas
import com.san.kir.data.models.main.SimplifiedManga
import com.san.kir.data.models.utils.SortLibraryUtil
import com.san.kir.data.settingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal class LibraryViewModel(
    private val mangaRepository: MangaRepository = ManualDI.mangaRepository(),
    categoryRepository: CategoryRepository = ManualDI.categoryRepository(),
    private val updateManager: UpdateMangaManager = ManualDI.updateMangaManager(),
    settingsRepository: SettingsRepository = ManualDI.settingsRepository(),
) : ViewModel<LibraryState>(), LibraryStateHolder {

    private val currentCategory = MutableStateFlow(CategoryWithMangas())
    private val backgroundState = MutableStateFlow(BackgroundState.None)
    private val itemsState = combine(categoryRepository.items, mangaRepository.simplifiedItems) { cats, mangas ->
        if (cats.isEmpty()) return@combine ItemsState.Empty
        ItemsState.Ok(
            items = cats.filter { it.isVisible }.map { transform(it, mangas) },
            categories = cats.associate { it.id to it.name }
        )
    }


    init {
        MangaDeleteWorker.workInfos()
            .onEach { works ->
                if (works.all { it.state.isFinished }) backgroundState.update { BackgroundState.None }
                else backgroundState.update { BackgroundState.Work }
            }
            .launch()
    }

    override val defaultState = LibraryState()
    override val tempState = combine(
        currentCategory,
        itemsState,
        settingsRepository.isShowCategory,
        backgroundState,
        ::LibraryState
    )

    override suspend fun onAction(action: Action) {
        when (action) {
            LibraryAction.UpdateApp -> AppUpdateWorker.addTask()
            LibraryAction.UpdateCurrentCategory -> updateManager.addTasks(currentCategory.value.mangas.map { it.id })
            is LibraryAction.SetCurrentCategory -> currentCategory.value = action.item
            is LibraryAction.DeleteManga -> MangaDeleteWorker.addTask(action.mangaId, action.withFiles)
            is LibraryAction.ChangeColor -> mangaRepository.changeColor(action.mangaId, action.color)

            is LibraryAction.ChangeCategory -> {
                mangaRepository.changeCategory(action.mangaId, action.categoryId)
                sendEvent(LibraryEvent.DismissSelectedMangaDialog)
            }

            LibraryAction.UpdateAll -> {
                val itemsState = state.value.items as? ItemsState.Ok ?: return
                updateManager.addTasks(itemsState.items.flatMap { it.mangas }.map { it.id })
            }
        }
    }

    private fun transform(cat: Category, mangas: List<SimplifiedManga>): CategoryWithMangas {
        val filteredMangas = mangas.filter {
            cat.name == ManualDI.categoryAll() || it.categoryId == cat.id
        }
        val sortedMangas = when (cat.typeSort) {
            SortLibraryUtil.ABC -> filteredMangas.sortedBy { it.name }
            SortLibraryUtil.ADD -> filteredMangas.sortedBy { it.id }
            SortLibraryUtil.POP -> filteredMangas.sortedBy { it.populate }
            else -> filteredMangas
        }
        return CategoryWithMangas(
            id = cat.id,
            name = cat.name,
            typeSort = cat.typeSort,
            isReverseSort = cat.isReverseSort,
            spanPortrait = cat.spanPortrait,
            spanLandscape = cat.spanLandscape,
            mangas = if (cat.isReverseSort) sortedMangas.asReversed() else sortedMangas
        )
    }

}

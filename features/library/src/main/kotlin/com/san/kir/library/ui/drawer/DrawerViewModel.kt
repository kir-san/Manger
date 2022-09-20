package com.san.kir.library.ui.drawer

import android.app.Application
import com.san.kir.background.works.UpdateMainMenuWorker
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.library.logic.repo.MainMenuRepository
import com.san.kir.library.logic.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class DrawerViewModel @Inject constructor(
    private val context: Application,
    settingsRepository: SettingsRepository,
    private val mainMenuRepository: MainMenuRepository,
) : BaseViewModel<DrawerEvent, DrawerState>() {
    override val tempState = combine(
        settingsRepository.main().mapLatest { it.editMenu },
        mainMenuRepository.items.onStart { UpdateMainMenuWorker.addTask(context) }
    ) { edit, menu ->
        DrawerState(edit, MainMenuItemsState.Ok(menu.toPersistentList()))
    }

    override val defaultState = DrawerState(
        hasEditMenu = false,
        menu = MainMenuItemsState.Load
    )

    override suspend fun onEvent(event: DrawerEvent) {
        when (event) {
            is DrawerEvent.Reorder -> mainMenuRepository.swap(event.from, event.to)
        }
    }
}

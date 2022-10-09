package com.san.kir.settings.ui.settings

import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.settings.logic.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<SettingsEvent, SettingsState>() {
    override val tempState = settingsRepository.settings().map {
        SettingsState(it.main, it.download, it.viewer, it.chapters)
    }

    override val defaultState = SettingsState()

    override suspend fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SaveChapters -> settingsRepository.update(event.state)
            is SettingsEvent.SaveDownload -> settingsRepository.update(event.state)
            is SettingsEvent.SaveMain -> settingsRepository.update(event.state)
            is SettingsEvent.SaveViewer -> settingsRepository.update(event.state)
        }
    }
}

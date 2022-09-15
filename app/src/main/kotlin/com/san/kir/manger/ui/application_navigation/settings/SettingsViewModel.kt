package com.san.kir.manger.ui.application_navigation.settings

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.compose_utils.view_models.mutableStateOf
import com.san.kir.core.compose_utils.view_models.snapshot
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.models.base.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    var theme by mutableStateOf(true, settingsRepository::setTheme)
    var showCategory by mutableStateOf(true, settingsRepository::setShowCategory)
    var editMenu by mutableStateOf(false, settingsRepository::setEditMenu)

    var concurrent by mutableStateOf(true, settingsRepository::setConcurrent)
    var retry by mutableStateOf(false, settingsRepository::setRetry)
    var wifi by mutableStateOf(false, settingsRepository::setWifi)

    var orientation by mutableStateOf(
        Settings.Viewer.Orientation.AUTO_LAND,
        settingsRepository::setOrientation
    )
    var cutout by mutableStateOf(true, settingsRepository::setCutOut)
    var control = mutableStateListOf(false, true, false)
    var withoutSaveFiles by mutableStateOf(false, settingsRepository::setWithoutSaveFiles)

    var title by mutableStateOf(true, settingsRepository::setTitle)
    var filter by mutableStateOf(true, settingsRepository::setFilter)

    init {
        viewModelScope.defaultLaunch {
            val main = settingsRepository.currentMain()
            val download = settingsRepository.currentDownload()
            val viewer = settingsRepository.currentViewer()
            val chapters = settingsRepository.currentChapters()

            withMainContext {
                theme = main.theme
                showCategory = main.isShowCategory
                editMenu = main.editMenu

                concurrent = download.concurrent
                retry = download.retry
                wifi = download.wifi

                orientation = viewer.orientation
                cutout = viewer.cutOut
                control[0] = viewer.control.taps
                control[1] = viewer.control.swipes
                control[2] = viewer.control.keys
                withoutSaveFiles = viewer.withoutSaveFiles

                title = chapters.isTitle
                filter = chapters.isIndividual
            }
        }

        snapshot(
            { (taps, swipes, keys) -> settingsRepository.setControl(taps, swipes, keys) }
        ) { control }
    }
}

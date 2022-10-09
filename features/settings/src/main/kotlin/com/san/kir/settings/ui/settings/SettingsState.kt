package com.san.kir.settings.ui.settings

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Settings


internal data class SettingsState(
    val main: Settings.Main = Settings.Main(),
    val download: Settings.Download = Settings.Download(),
    val viewer: Settings.Viewer = Settings.Viewer(),
    val chapters: Settings.Chapters = Settings.Chapters()
) : ScreenState

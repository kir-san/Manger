package com.san.kir.settings.ui.settings

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.base.Settings


internal sealed interface SettingsEvent : ScreenEvent {
    data class SaveMain(val state: Settings.Main) : SettingsEvent
    data class SaveDownload(val state: Settings.Download) : SettingsEvent
    data class SaveViewer(val state: Settings.Viewer) : SettingsEvent
    data class SaveChapters(val state: Settings.Chapters) : SettingsEvent
}

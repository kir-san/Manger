package com.san.kir.settings.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.data.models.base.Settings
import com.san.kir.settings.R
import com.san.kir.settings.utils.ListPreferenceItem
import com.san.kir.settings.utils.MultiSelectListPreferenceItem
import com.san.kir.settings.utils.PreferenceTitle
import com.san.kir.settings.utils.TogglePreferenceItem
import kotlinx.collections.immutable.toPersistentList

@Composable
fun SettingsScreen(
    navigateUp: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenContent(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.main_menu_settings),
        ),
        additionalPadding = Dimensions.zero
    ) {
        Column(modifier = Modifier.bottomInsetsPadding()) {
            Main(state.main, viewModel::sendEvent)
            Divider()

            Chapters(state.chapters, viewModel::sendEvent)
            Divider()

            Viewer(state.viewer, viewModel::sendEvent)
            Divider()

            Download(state.download, viewModel::sendEvent)
        }
    }
}

@Composable
private fun Main(main: Settings.Main, sendEvent: (SettingsEvent.SaveMain) -> Unit) {
    TogglePreferenceItem(
        title = R.string.settings_app_dark_theme_title,
        subtitle = R.string.settings_app_dark_theme_summary,
        icon = Icons.Default.DarkMode,
        initialValue = main.theme,
        onCheckedChange = { sendEvent(SettingsEvent.SaveMain(main.copy(theme = it))) }
    )

    TogglePreferenceItem(
        title = R.string.settings_app_edit_menu_title,
        subtitle = R.string.settings_app_edit_menu_summary,
        initialValue = main.editMenu,
        onCheckedChange = { sendEvent(SettingsEvent.SaveMain(main.copy(editMenu = it))) }
    )

    Divider()

    PreferenceTitle(R.string.settings_library_title)

    TogglePreferenceItem(
        title = R.string.settings_library_show_category_title,
        subtitle = R.string.settings_library_show_category_summary,
        icon = Icons.Default.Category,
        initialValue = main.isShowCategory,
        onCheckedChange = { sendEvent(SettingsEvent.SaveMain(main.copy(isShowCategory = it))) }
    )
}

@Composable
private fun Chapters(chapters: Settings.Chapters, sendEvent: (SettingsEvent.SaveChapters) -> Unit) {
    PreferenceTitle(R.string.settings_list_chapter_title)

    TogglePreferenceItem(
        title = R.string.settings_list_chapter_filter_title,
        subtitle = R.string.settings_list_chapter_filter_summary,
        icon = Icons.Default.FilterList,
        initialValue = chapters.isIndividual,
        onCheckedChange = { sendEvent(SettingsEvent.SaveChapters(chapters.copy(isIndividual = it))) }
    )

    TogglePreferenceItem(
        title = R.string.settings_list_chapter_title_title,
        subtitle = R.string.settings_list_chapter_title_summary,
        icon = Icons.Default.Title,
        initialValue = chapters.isTitle,
        onCheckedChange = { sendEvent(SettingsEvent.SaveChapters(chapters.copy(isTitle = it))) }
    )
}

@Composable
private fun Viewer(viewer: Settings.Viewer, sendEvent: (SettingsEvent.SaveViewer) -> Unit) {
    PreferenceTitle(R.string.settings_viewer_title)

    ListPreferenceItem(
        title = R.string.settings_viewer_orientation_title,
        subtitle = R.string.settings_viewer_orientation_summary,
        icon = Icons.Default.CropLandscape,
        entries = R.array.settings_viewer_orientation_array,
        entryValues = Settings.Viewer.Orientation.values().toList().toPersistentList(),
        initialValue = viewer.orientation,
        onValueChange = { sendEvent(SettingsEvent.SaveViewer(viewer.copy(orientation = it))) }
    )

    MultiSelectListPreferenceItem(
        title = R.string.settings_viewer_control_title,
        subtitle = R.string.settings_viewer_control_summary,
        icon = Icons.Default.VideogameAsset,
        entries = R.array.settings_viewer_control_array,
        initialValue = viewer.controls.toPersistentList(),
        onValueChange = {
            sendEvent(SettingsEvent.SaveViewer(viewer.copy(control = viewer.controls(it))))
        }
    )

    TogglePreferenceItem(
        title = R.string.settings_viewer_cutout_title,
        subtitle = R.string.settings_viewer_cutout_summary,
        icon = Icons.Default.ContentCut,
        initialValue = viewer.cutOut,
        onCheckedChange = { sendEvent(SettingsEvent.SaveViewer(viewer.copy(cutOut = it))) }
    )

    TogglePreferenceItem(
        title = R.string.settings_viewer_without_title,
        subtitle = R.string.settings_viewer_without_summary,
//            icon = Icons.Default.ContentCut,
        initialValue = viewer.withoutSaveFiles,
        onCheckedChange = { sendEvent(SettingsEvent.SaveViewer(viewer.copy(withoutSaveFiles = it))) }
    )

}

@Composable
private fun Download(download: Settings.Download, sendEvent: (SettingsEvent.SaveDownload) -> Unit) {
    PreferenceTitle(R.string.settings_downloader_title)

    TogglePreferenceItem(
        title = R.string.settings_downloader_parallel_title,
        subtitle = R.string.settings_downloader_parallel_summary,
        icon = Icons.Default.CompareArrows,
        initialValue = download.concurrent,
        onCheckedChange = { sendEvent(SettingsEvent.SaveDownload(download.copy(concurrent = it))) }
    )

    TogglePreferenceItem(
        title = R.string.settings_downloader_retry_title,
        subtitle = R.string.settings_downloader_retry_summary,
        icon = Icons.Default.ErrorOutline,
        initialValue = download.retry,
        onCheckedChange = { sendEvent(SettingsEvent.SaveDownload(download.copy(retry = it))) }
    )

    TogglePreferenceItem(
        title = R.string.settings_downloader_wifi_only_title,
        subtitle = R.string.settings_downloader_wifi_only_summary,
        icon = Icons.Default.Wifi,
        initialValue = download.wifi,
        onCheckedChange = { sendEvent(SettingsEvent.SaveDownload(download.copy(wifi = it))) }
    )
}

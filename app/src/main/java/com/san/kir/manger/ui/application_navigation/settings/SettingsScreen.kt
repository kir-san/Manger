package com.san.kir.manger.ui.application_navigation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.Viewer
import com.san.kir.manger.ui.utils.CheckBoxText
import com.san.kir.manger.ui.utils.RadioGroup
import com.san.kir.manger.ui.utils.TopBarScreenContent

@Composable
fun SettingsScreen(
    nav: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    TopBarScreenContent(
        navHostController = nav,
        title = stringResource(R.string.main_menu_settings),
        additionalPadding = 0.dp
    ) {

        TogglePreferenceItem(
            title = R.string.settings_app_dark_theme_title,
            subtitle = R.string.settings_app_dark_theme_summary,
            icon = Icons.Default.DarkMode,
            initialValue = viewModel.theme,
            onCheckedChange = { viewModel.theme = it }
        )

        TogglePreferenceItem(
            title = R.string.settings_app_edit_menu_title,
            subtitle = R.string.settings_app_edit_menu_summary,
            initialValue = viewModel.editMenu,
            onCheckedChange = { viewModel.editMenu = it })

        Divider()

        PreferenceTitle(R.string.settings_list_chapter_title)

        TogglePreferenceItem(
            title = R.string.settings_list_chapter_filter_title,
            subtitle = R.string.settings_list_chapter_filter_summary,
            icon = Icons.Default.FilterList,
            initialValue = viewModel.filter,
            onCheckedChange = { viewModel.filter = it }
        )

        TogglePreferenceItem(
            title = R.string.settings_list_chapter_title_title,
            subtitle = R.string.settings_list_chapter_title_summary,
            icon = Icons.Default.Title,
            initialValue = viewModel.title,
            onCheckedChange = { viewModel.title = it }
        )

        Divider()

        PreferenceTitle(R.string.settings_library_title)

        TogglePreferenceItem(
            title = R.string.settings_library_show_category_title,
            subtitle = R.string.settings_library_show_category_summary,
            icon = Icons.Default.Category,
            initialValue = viewModel.showCategory,
            onCheckedChange = { viewModel.showCategory = it }
        )
        Divider()

        PreferenceTitle(R.string.settings_viewer_title)

        ListPreferenceItem(
            title = R.string.settings_viewer_orientation_title,
            subtitle = R.string.settings_viewer_orientation_summary,
            icon = Icons.Default.CropLandscape,
            entries = R.array.settings_viewer_orientation_array,
            entryValues = Viewer.Orientation.values().toList(),
            initialValue = viewModel.orientation,
            onValueChange = { viewModel.orientation = it }
        )

        MultiSelectListPreferenceItem(
            title = R.string.settings_viewer_control_title,
            subtitle = R.string.settings_viewer_control_summary,
            icon = Icons.Default.VideogameAsset,
            entries = R.array.settings_viewer_control_array,
            value = viewModel.control,
        )

        TogglePreferenceItem(
            title = R.string.settings_viewer_cutout_title,
            subtitle = R.string.settings_viewer_cutout_summary,
            icon = Icons.Default.ContentCut,
            initialValue = viewModel.cutout,
            onCheckedChange = { viewModel.cutout = it }
        )

        TogglePreferenceItem(
            title = R.string.settings_viewer_without_title,
            subtitle = R.string.settings_viewer_without_summary,
//            icon = Icons.Default.ContentCut,
            initialValue = viewModel.withoutSaveFiles,
            onCheckedChange = { viewModel.withoutSaveFiles = it }
        )

        Divider()

        PreferenceTitle(R.string.settings_downloader_title)

        TogglePreferenceItem(
            title = R.string.settings_downloader_parallel_title,
            subtitle = R.string.settings_downloader_parallel_summary,
            icon = Icons.Default.CompareArrows,
            initialValue = viewModel.concurrent,
            onCheckedChange = { viewModel.concurrent = it }
        )

        TogglePreferenceItem(
            title = R.string.settings_downloader_retry_title,
            subtitle = R.string.settings_downloader_retry_summary,
            icon = Icons.Default.ErrorOutline,
            initialValue = viewModel.retry,
            onCheckedChange = { viewModel.retry = it }
        )

        TogglePreferenceItem(
            title = R.string.settings_downloader_wifi_only_title,
            subtitle = R.string.settings_downloader_wifi_only_summary,
            icon = Icons.Default.Wifi,
            initialValue = viewModel.wifi,
            onCheckedChange = { viewModel.wifi = it }
        )
    }
}

@Composable
fun <T> ListPreferenceItem(
    title: Int,
    subtitle: Int,
    icon: ImageVector? = null,
    entries: Int,
    entryValues: List<T>,
    initialValue: T,
    onValueChange: (T) -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }
    TemplatePreferenceItem(title = title, subtitle = subtitle, icon = icon) {
        dialog = true
    }

    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            title = {
                Text(stringResource(title))
            },
            text = {
                RadioGroup(
                    state = initialValue,
                    onSelected = {
                        onValueChange(it)
                        dialog = false
                    },
                    stateList = entryValues,
                    textList = stringArrayResource(entries).toList(),
                    verticalPadding = 8.dp,
                )
            },
            buttons = {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                        onClick = { dialog = false }) {
                        Text("CANCEL")
                    }
                }
            }
        )
    }
}

@Composable
fun MultiSelectListPreferenceItem(
    title: Int,
    subtitle: Int,
    icon: ImageVector? = null,
    entries: Int,
    value: MutableList<Boolean>,
) {
    var dialog by remember { mutableStateOf(false) }
    TemplatePreferenceItem(title = title, subtitle = subtitle, icon = icon) {
        dialog = true
    }

    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            title = {
                Text(stringResource(title))
            },
            text = {
                val textList = stringArrayResource(entries).toList()

                Column {
                    textList.forEachIndexed { index, text ->
                        CheckBoxText(
                            state = value[index],
                            onChange = { value[index] = it },
                            firstText = text,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            },
            buttons = {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                        onClick = { dialog = false }) {
                        Text("CLOSE")
                    }
                }
            }
        )
    }
}


@Composable
fun TogglePreferenceItem(
    title: Int,
    subtitle: Int,
    icon: ImageVector? = null,
    initialValue: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {

    TemplatePreferenceItem(
        title = title, subtitle = subtitle,
        icon = icon,
        action = {
            Switch(
                checked = initialValue,
                onCheckedChange = { onCheckedChange(it) })
        }) {

    }
}

@Composable
fun PreferenceTitle(id: Int) {
    Column {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 64.dp, bottom = 8.dp, top = 16.dp)
        ) {
            Text(
                text = stringResource(id),
//                fontSize = 14.sp,
                color = MaterialTheme.colors.secondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun TemplatePreferenceItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: Int,
    subtitle: Int,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Icon(icon, contentDescription = "")
                    }
                }
            }
            Column(
                modifier = Companion.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.subtitle1) {
                    Text(stringResource(title))
                }
                Spacer(modifier = Modifier.size(2.dp))
                ProvideTextStyle(value = MaterialTheme.typography.caption) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(stringResource(subtitle))
                    }
                }
            }
        }

        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (action != null)
                action()
        }
    }
}

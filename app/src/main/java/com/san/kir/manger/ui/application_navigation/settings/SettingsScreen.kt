package com.san.kir.manger.ui.application_navigation.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.ui.utils.RadioGroup
import com.san.kir.manger.ui.utils.TopBarScreenContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        val theme by viewModel.theme.collectAsState()
        TogglePreferenceItem(
            title = R.string.settings_app_dark_theme_title,
            subtitle = R.string.settings_app_dark_theme_summary,
            initialValue = theme,
        ) { viewModel.setTheme(it) }
        Divider()

    }
}

@Composable
fun ListPreferenceItem(
    title: Int,
    subtitle: Int,
    entries: Int,
    entryValues: Int,
    initialValue: String,
    onValueChange: (String) -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }
    TemplatePreferenceItem(title = title, subtitle = subtitle) {
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
                    onSelected = { onValueChange(it) },
                    stateList = stringArrayResource(entryValues).toList(),
                    textList = stringArrayResource(entries).toList()
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
fun TogglePreferenceItem(
    title: Int,
    subtitle: Int,
    initialValue: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {

    TemplatePreferenceItem(
        title = title, subtitle = subtitle,
        action = {
            Switch(
                checked = initialValue,
                onCheckedChange = { onCheckedChange(it) })
        }) {

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
        modifier = modifier.fillMaxWidth(),
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
        if (action != null) {
            Divider(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(56.dp)
                    .width(1.dp),
            )
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                action()
            }
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    ctx: Application,
    private val main: MainRepository,
) : ViewModel() {
    private val _theme = MutableStateFlow(true)
    val theme = _theme.asStateFlow()

    fun setTheme(value: Boolean) = viewModelScope.launch {
        main.setTheme(value)
    }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            main.data
                .collect { data ->
                    _theme.update { data.theme }
                }
        }
    }
}

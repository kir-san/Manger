package com.san.kir.settings.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.CheckBoxText
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.RadioGroup
import com.san.kir.core.compose.SmallestSpacer
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.startInsetsPadding
import com.san.kir.settings.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
private fun TemplatePreferenceItem(
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
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.half),
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = modifier
                    .startInsetsPadding()
                    .size(Dimensions.Image.bigger),
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Icon(icon, contentDescription = "")
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(title), style = MaterialTheme.typography.subtitle1)

                SmallestSpacer()

                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(stringResource(subtitle), style = MaterialTheme.typography.caption)
                }
            }
        }

        Box(
            modifier = Modifier
                .endInsetsPadding()
                .size(Dimensions.Image.bigger),
            contentAlignment = Alignment.Center,
        ) {
            if (action != null)
                action()
        }
    }
}

@Composable
internal fun PreferenceTitle(id: Int) {
    Column {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimensions.Image.bigger,
                    bottom = Dimensions.half,
                    top = Dimensions.default
                )
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
internal fun <T> ListPreferenceItem(
    title: Int,
    subtitle: Int,
    icon: ImageVector? = null,
    entries: Int,
    entryValues: ImmutableList<T>,
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
                    textList = stringArrayResource(entries).toList().toImmutableList(),
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
internal fun TogglePreferenceItem(
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
        },
        onClick = {
            onCheckedChange(initialValue.not())
        }
    )
}

@Composable
internal fun MultiSelectListPreferenceItem(
    title: Int,
    subtitle: Int,
    icon: ImageVector? = null,
    entries: Int,
    initialValue: ImmutableList<Boolean>,
    onValueChange: (List<Boolean>) -> Unit
) {
    val items = remember(initialValue) { mutableStateListOf(*initialValue.toTypedArray()) }

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
                            state = items[index],
                            onChange = { items[index] = it },
                            firstText = text,
                            modifier = Modifier.padding(vertical = Dimensions.half)
                        )
                    }
                }
            },
            buttons = {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.padding(
                            bottom = Dimensions.default,
                            end = Dimensions.default
                        ),
                        onClick = {
                            onValueChange(items)
                            dialog = false
                        }
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        )
    }
}

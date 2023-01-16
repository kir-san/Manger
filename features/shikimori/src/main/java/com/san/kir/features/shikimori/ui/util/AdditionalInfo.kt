package com.san.kir.features.shikimori.ui.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.Fonts
import com.san.kir.core.compose.TextProgress
import com.san.kir.core.compose.TextWithFirstWordBold
import com.san.kir.core.compose.rememberImage
import com.san.kir.data.models.base.ShikimoriGenre
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.data.models.base.ShikimoriStatus
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.accountRate.ProfileState

// Дополнительная информация о манге
@Composable
internal fun AdditionalInfo(
    manga: ShikimoriManga,
    state: ProfileState,
    onChange: (ShikimoriRate) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.default)
    ) {
        // Лого
        Image(
            rememberImage(manga.image.original),
            contentDescription = "manga logo",
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = Dimensions.default),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(3f)) {
            Status(state)

            // Количество томов
            Volumes(manga.volumes)

            // Количество глав
            Chapters(all = manga.chapters, state = state)

            // Рейтинг
            Score(manga.score, state = state)

            // Жанры
            Genres(manga.genres)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                ChangeButton(state, manga, onChange)
            }
        }
    }
}


@Composable
private fun Status(state: ProfileState) {
    when (state) {
        ProfileState.Load -> TextProgress()
        ProfileState.None -> {}
        is ProfileState.Ok -> {
            val statuses = LocalContext.current.resources.getStringArray(R.array.statuses)

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextWithFirstWordBold(
                    stringResource(R.string.current_status, statuses[state.rate.status.ordinal]),
                )
                if (state.rate.rewatches > 1)
                    Text(
                        " - ${state.rate.rewatches}",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
            }
        }
    }
}

@Composable
private fun Volumes(value: Long?) {
    value?.let { volume ->
        TextWithFirstWordBold(
            stringResource(R.string.profile_item_volumes, volume),
        )
    }
}

@Composable
private fun Chapters(all: Long, state: ProfileState) {
    when (state) {
        ProfileState.Load ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextWithFirstWordBold(
                    stringResource(R.string.profile_item_chapters, all)
                )
                TextProgress()
            }

        ProfileState.None ->
            TextWithFirstWordBold(
                stringResource(R.string.profile_item_chapters, all)
            )

        is ProfileState.Ok ->
            TextWithFirstWordBold(
                stringResource(R.string.reading, state.rate.chapters, all),
                modifier = Modifier.fillMaxWidth()
            )
    }
}

@Composable
internal fun Chapters(all: Long, read: Long) {
    TextWithFirstWordBold(
        stringResource(R.string.reading, read, all),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Score(mangaScore: Float?, state: ProfileState) {
    when (state) {
        ProfileState.Load ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                mangaScore?.let { mScore ->
                    TextWithFirstWordBold(stringResource(R.string.profile_item_score, mScore))
                }
                TextProgress()
            }

        ProfileState.None ->
            mangaScore?.let { mScore ->
                TextWithFirstWordBold(stringResource(R.string.profile_item_score, mScore))
            }

        is ProfileState.Ok ->
            TextWithFirstWordBold(
                stringResource(
                    R.string.profile_item_score,
                    state.rate.score.toFloat()
                )
            )
    }
}

@Composable
private fun Genres(items: List<ShikimoriGenre>) {
    if (items.isNotEmpty())
        TextWithFirstWordBold(
            stringResource(
                R.string.profile_item_genres,
                items.map { it.russian }.toString().removeSurrounding("[", "]")
            )
        )
}

@Composable
private fun ChangeButton(
    state: ProfileState,
    manga: ShikimoriManga,
    onChange: (ShikimoriRate) -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }
    var tempRate by remember { mutableStateOf<ShikimoriRate?>(null) }

    AnimatedVisibility(state is ProfileState.Ok) {
        OutlinedButton(
            onClick = {
                tempRate = (state as ProfileState.Ok).rate
                dialog = true
            },
        ) {
            Text(stringResource(R.string.change))
        }
    }

    if (dialog)
        Dialog(
            onDismiss = { dialog = false },
            buttons = {
                TextButton(onClick = {
                    dialog = false
                    tempRate?.let(onChange)
                }) {
                    Text(stringResource(R.string.change))
                }
            }
        ) {
            ChangeDialogContent(tempRate, manga) { tempRate = it }
        }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Dialog(
    onDismiss: () -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier.padding(horizontal = Dimensions.big),
        ) {
            Column(modifier = Modifier.padding(Dimensions.default)) {
                content()

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .padding(top = Dimensions.default)
                        .fillMaxWidth()
                ) {
                    buttons()
                }
            }
        }
    }
}

@Composable
private fun ChangeDialogContent(
    rate: ShikimoriRate?,
    manga: ShikimoriManga,
    onChange: (ShikimoriRate) -> Unit,
) {
    if (rate != null) {
        BoxWithConstraints {
            val textWidth = maxWidth * 0.4f
            Column {
                StatusChanger(
                    status = rate.status,
                    onChange = { onChange(rate.copy(status = it)) },
                )

                DefaultSpacer()

                WithLabel(R.string.profile_item_chapters_change, textWidth) {
                    CountChanger(
                        initialCount = rate.chapters,
                        maxCount = manga.chapters,
                        onChange = {
                            onChange(
                                rate.copy(
                                    chapters = it,
                                    status = ShikimoriStatus.Watching
                                )
                            )
                        },
                    )
                }

                DefaultSpacer()

                WithLabel(R.string.profile_item_rewrite_change, textWidth) {
                    CountChanger(
                        initialCount = rate.rewatches,
                        onChange = { onChange(rate.copy(rewatches = it)) },
                    )
                }

                DefaultSpacer()

                WithLabel(R.string.profile_item_score_change, textWidth) {
                    CountChanger(
                        initialCount = rate.score,
                        maxCount = 10,
                        onChange = { onChange(rate.copy(score = it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WithLabel(textRes: Int, width: Dp, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(id = textRes),
            modifier = Modifier
                .width(width)
                .padding(end = Dimensions.default),
            textAlign = TextAlign.End
        )
        content()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StatusChanger(
    status: ShikimoriStatus,
    onChange: (ShikimoriStatus) -> Unit,
) {
    val statuses = LocalContext.current.resources.getStringArray(R.array.statuses)
    var expandMenu by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf(status) }
    ExposedDropdownMenuBox(
        expanded = expandMenu,
        onExpandedChange = { expandMenu = expandMenu.not() },
    ) {
        OutlinedTextField(
            value = statuses[currentStatus.ordinal],
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandMenu) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            label = { Text(text = "Статус") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandMenu,
            onDismissRequest = { expandMenu = false },
        ) {
            ShikimoriStatus.values().forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        currentStatus = item
                        onChange(item)
                        expandMenu = false
                    },
                ) {
                    Text(text = statuses[item.ordinal])
                }
            }
        }
    }
}

@Composable
private fun CountChanger(
    initialCount: Long,
    maxCount: Long? = null,
    onChange: (Long) -> Unit,
) {
    var counter by remember { mutableStateOf(initialCount) }
    var isWrongText by remember { mutableStateOf(false) }

    onChange(counter)

    fun prepareCounter(newValue: Long) {
        var tempValue = maxOf(0, newValue)
        maxCount?.let { max -> tempValue = minOf(tempValue, max) }

        counter = tempValue
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { prepareCounter(counter - 1) }) {
            Icon(Icons.Default.Remove, contentDescription = "Decrement")
        }

        OutlinedTextField(
            value = "$counter",
            onValueChange = {
                kotlin.runCatching {
                    isWrongText = false
                    prepareCounter(initialCount)
                }.onFailure { isWrongText = true }
            },
            isError = isWrongText,
            modifier = Modifier.weight(1f)
        )

        maxCount?.let { Text(text = " / $maxCount", fontSize = Fonts.Size.bigger) }

        IconButton(onClick = { prepareCounter(counter + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "Increment")
        }
    }
}

@Preview
@Composable
internal fun CountChangerPreview() {
    MaterialTheme {
        CountChanger(initialCount = 4, onChange = {}, maxCount = 10)
    }
}


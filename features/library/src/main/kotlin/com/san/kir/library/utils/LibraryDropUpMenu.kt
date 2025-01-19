package com.san.kir.library.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.ColorPickerState
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.compose.animation.rememberSharedParams
import com.san.kir.core.compose.animation.saveParams
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.rememberColorPickerState
import com.san.kir.core.utils.remove
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.returned
import com.san.kir.data.models.main.SimplifiedManga
import com.san.kir.library.R
import com.san.kir.library.ui.library.ItemsState
import com.san.kir.library.ui.library.LibraryAction
import com.san.kir.library.ui.library.LibraryEvent


private val ItemCornerShape = RoundedCornerShape(Dimensions.default)
private val ButtonPadding = Dimensions.quarter

private val expandedItemModifier: Modifier
    @Composable get() = Modifier
        .background(MaterialTheme.colorScheme.surfaceVariant, ItemCornerShape)


private val expandedItemContentColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant


internal enum class Expanded { COLOR, CATEGORY, DELETE, NONE; }

@Composable
internal fun LibraryDropUpMenu(
    itemsState: ItemsState,
    selectedManga: SimplifiedManga,
    sendAction: (Action) -> Unit,
) {
    val expandedState = rememberSaveable { mutableStateOf(Expanded.NONE) }
    val defaultColor = MaterialTheme.colorScheme.primary
    val colorState = rememberColorPickerState(selectedManga.composeColor(defaultColor))

    Column {
        Title { sendAction(LibraryEvent.DismissSelectedMangaDialog.returned()) }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .bottomInsetsPadding()
        ) {
            SubTitle(selectedManga.name, selectedManga.logo, colorState.currentColor) {
                expandedState.value =
                    if (expandedState.value == Expanded.COLOR) Expanded.NONE else Expanded.COLOR
            }

            ColorPicker(
                colorState = colorState,
                visibility = expandedState.value == Expanded.COLOR,
                onApply = {
                    expandedState.value = Expanded.NONE
                    sendAction(LibraryAction.ChangeColor(selectedManga.id, colorState.currentColor.toArgb()))
                },
                onCancel = {
                    expandedState.value = Expanded.NONE
                    colorState.reset()
                }
            )

            HorizontalDivider(modifier = Modifier.horizontalInsetsPadding(Dimensions.default))

            Properties { params ->
                sendAction(LibraryEvent.ToInfo(selectedManga.id, params).returned())
            }

            CategoryChanger(
                changerVisibility = expandedState.value == Expanded.CATEGORY,
                itemsState = itemsState,
                selectedManga = selectedManga,
                onClick = {
                    expandedState.value =
                        if (expandedState.value == Expanded.CATEGORY) Expanded.NONE else Expanded.CATEGORY
                },
                onItemClick = { categoryId ->
                    expandedState.value = Expanded.NONE
                    sendAction(LibraryAction.ChangeCategory(selectedManga.id, categoryId))
                }
            )

            Storage { params ->
                sendAction(LibraryEvent.ToStorage(selectedManga.id, params).returned())
            }

            Statistics { params ->
                sendAction(LibraryEvent.ToStats(selectedManga.id, params).returned())
            }

            Delete(
                changerVisibility = expandedState.value == Expanded.DELETE,
                onClick = {
                    expandedState.value =
                        if (expandedState.value == Expanded.DELETE) Expanded.NONE else Expanded.DELETE
                },
                onDismiss = {
                    expandedState.value = Expanded.NONE
                    it?.let { sendAction(LibraryAction.DeleteManga(selectedManga.id, it)) }
                    sendAction(LibraryEvent.DismissSelectedMangaDialog.returned())
                },
            )
        }
    }
}

@Composable
private fun Title(onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.selected_manga),
            modifier = Modifier
                .padding(Dimensions.default)
                .weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        IconButton(onClick = onClick, modifier = Modifier.padding(end = Dimensions.half)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
private fun SubTitle(
    text: String,
    logo: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalInsetsPadding()
    ) {
        LogoImage(
            logo = logo,
            modifier = Modifier
                .padding(Dimensions.default)
                .size(Dimensions.Image.bigger)
        )

        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            fontWeight = FontWeight.Bold,
            maxLines = 3
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = Dimensions.default)
                .clip(RoundedCornerShape(50))
                .background(color = color)
                .size(Dimensions.Image.small)
                .clickable(onClick = onClick)
        )
    }
}


@Composable
private fun Properties(onClick: (SharedParams) -> Unit) {
    val params = rememberSharedParams()
    DropdownMenuItem(
        text = { Text(stringResource(R.string.properties)) },
        onClick = { onClick(params) },
        modifier = Modifier
            .horizontalInsetsPadding()
            .saveParams(params)
    )
}

@Composable
private fun ColumnScope.CategoryChanger(
    changerVisibility: Boolean,
    itemsState: ItemsState,
    selectedManga: SimplifiedManga,
    onClick: () -> Unit,
    onItemClick: (Long) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(R.string.change_category)) },
        onClick = onClick,
        modifier = Modifier.horizontalInsetsPadding(),
        trailingIcon = {
            Text(
                text = selectedManga.category,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, ItemCornerShape)
                    .padding(Dimensions.half)
            )
        }
    )

    if (itemsState is ItemsState.Ok)
        ExpandedCategories(
            visibility = changerVisibility,
            categories = itemsState.categories.remove(selectedManga.categoryId),
            onItemChanged = onItemClick
        )
}

@Composable
private fun ColumnScope.ExpandedCategories(
    visibility: Boolean,
    categories: Map<Long, String>,
    onItemChanged: (Long) -> Unit,
) {

    BottomAnimatedVisibility(
        visible = visibility,
        modifier = expandedItemModifier,
    ) {
        Column {
            categories.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(text = value, color = expandedItemContentColor) },
                    onClick = { onItemChanged(key) },
                    modifier = Modifier.horizontalInsetsPadding()
                )
            }
        }
    }
}

@Composable
private fun Storage(onClick: (SharedParams) -> Unit) {
    val params = rememberSharedParams()
    DropdownMenuItem(
        text = { Text(stringResource(R.string.used_memory)) },
        onClick = { onClick(params.copy()) },
        modifier = Modifier
            .horizontalInsetsPadding()
            .saveParams(params)
    )
}

@Composable
private fun Statistics(onClick: (SharedParams) -> Unit) {
    val params = rememberSharedParams()
    DropdownMenuItem(
        text = { Text(stringResource(R.string.read_statistic)) },
        onClick = { onClick(params.copy()) },
        modifier = Modifier
            .horizontalInsetsPadding()
            .saveParams(params)
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Delete(
    changerVisibility: Boolean,
    onClick: () -> Unit,
    onDismiss: (Boolean?) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.delete)) },
        onClick = onClick,
        modifier = Modifier.horizontalInsetsPadding()
    )

    BottomAnimatedVisibility(
        visible = changerVisibility,
        modifier = expandedItemModifier
    ) {

        Column(modifier = Modifier.horizontalInsetsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = Dimensions.Items.height)
                    .padding(Dimensions.default),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.really_delete_manga),
                    fontWeight = FontWeight.Bold,
                    color = expandedItemContentColor
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                OutlinedButton(
                    onClick = { onDismiss(true) },
                    modifier = Modifier.padding(ButtonPadding),
                ) {
                    Text(stringResource(R.string.really_ok_with_files))
                }

                OutlinedButton(
                    onClick = { onDismiss(false) },
                    modifier = Modifier.padding(ButtonPadding)
                ) {
                    Text(stringResource(R.string.really_ok))
                }

                OutlinedButton(
                    onClick = { onDismiss(null) },
                    modifier = Modifier.padding(ButtonPadding)
                ) {
                    Text(stringResource(R.string.never))
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    colorState: ColorPickerState,
    visibility: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    BottomAnimatedVisibility(visible = visibility, modifier = expandedItemModifier) {
        Column {
            com.san.kir.core.compose.ColorPicker(
                state = colorState,
                modifier = Modifier.horizontalInsetsPadding(horizontal = Dimensions.quarter)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = Dimensions.half)
                    .endInsetsPadding(Dimensions.half)
            ) {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = onApply) {
                    Text(text = stringResource(com.san.kir.catalog.R.string.apply))
                }
            }
        }
    }
}

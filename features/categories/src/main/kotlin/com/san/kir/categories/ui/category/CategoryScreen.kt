package com.san.kir.categories.ui.category

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.categories.R
import com.san.kir.core.compose.CheckBoxText
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.RadioGroup
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.SmallSpacer
import com.san.kir.core.compose.topBar
import com.san.kir.core.support.SortLibraryUtil
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

@Composable
fun CategoryScreen(
    navigateUp: () -> Unit,
    categoryName: String,
) {
    val viewModel: CategoryViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(categoryName) { viewModel.sendEvent(CategoryEvent.Set(categoryName)) }

    var hasError by rememberSaveable { mutableStateOf(false) }
    var deleteDialog by rememberSaveable { mutableStateOf(false) }

    ScreenContent(
        topBar = topBar(
            title = stringResource(
                if (state.hasCreatedNew) R.string.category_dialog_title_create
                else R.string.category_dialog_title_edit
            ),
            navigationButton = NavigationButton.Back(navigateUp),
            actions = {
                if (state.hasChanges and hasError.not())
                    MenuIcon(
                        icon = if (state.hasCreatedNew) Icons.Default.Create else Icons.Default.Save,
                    ) {
                        viewModel.sendEvent(CategoryEvent.Save)
                    }
                // Удаление категории полностью
                if (state.hasAll.not())
                    MenuIcon(icon = Icons.Default.Delete) {
                        deleteDialog = true
                    }
            }
        )
    ) {
        TextWithValidate(
            categoryName = state.category.name,
            categoryNames = state.categoryNames,
            hasAll = state.hasAll,
            hasError = hasError,
            sendEvent = viewModel::sendEvent,
            changeHasError = { hasError = it }
        )

        DefaultSpacer()

        Text(stringResource(R.string.category_dialog_sort))

        ChangeSortType(state.category.typeSort, viewModel::sendEvent)

        DefaultSpacer()

        ChangeReverseSort(state.category.isReverseSort, viewModel::sendEvent)

        DefaultSpacer()

        Text(stringResource(R.string.category_dialog_visible))

        ChangeVisibility(state.category.isVisible, viewModel::sendEvent)

        DefaultSpacer()

        Text(stringResource(R.string.category_dialog_portrait))

        SmallSpacer()

        ChangePortraitOptions(
            isLarge = state.category.isLargePortrait,
            span = state.category.spanPortrait,
            sendEvent = viewModel::sendEvent
        )

        DefaultSpacer()

        Text(stringResource(R.string.category_dialog_landscape))

        SmallSpacer()

        ChangeLandscapeOptions(
            isLarge = state.category.isLargeLandscape,
            span = state.category.spanLandscape,
            sendEvent = viewModel::sendEvent
        )
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            text = { Text(stringResource(R.string.category_item_question_delete)) },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.sendEvent(CategoryEvent.Delete)
                        navigateUp()
                    }
                ) {
                    Text(stringResource(R.string.category_item_question_delete_yes))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteDialog = false }
                ) {
                    Text(stringResource(R.string.category_item_question_delete_no))
                }
            }
        )
    }
}

@Composable
private fun TextWithValidate(
    categoryName: String,
    categoryNames: ImmutableList<String>,
    hasAll: Boolean,
    hasError: Boolean,
    sendEvent: (CategoryEvent) -> Unit,
    changeHasError: (Boolean) -> Unit,
) {
    val tooShortString = stringResource(R.string.category_dialog_validate_length)
    val nameIsBusyString = stringResource(R.string.category_dialog_validate_contain)

    var validate by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        value = categoryName,
        onValueChange = {
            changeHasError(true)
            validate = when {
                it.length < 3 -> tooShortString
                categoryNames.contains(it) -> nameIsBusyString
                else -> {
                    changeHasError(false)
                    ""
                }
            }
            sendEvent(CategoryEvent.Update(newName = it))
        },
        enabled = hasAll.not(),
        placeholder = { Text(stringResource(R.string.category_dialog_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        isError = hasError
    )
    AnimatedVisibility(visible = hasError && hasAll.not()) {
        Text(
            text = validate,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Red,
        )
    }
}

@Composable
private fun ChangeSortType(typeSort: String, sendEvent: (CategoryEvent) -> Unit) {
    RadioGroup(
        typeSort,
        onSelected = { sendEvent(CategoryEvent.Update(newTypeSort = it)) },
        stateList = listOf(
            SortLibraryUtil.add,
            SortLibraryUtil.abc,
            SortLibraryUtil.pop
        ),
        textList = listOf(
            R.string.library_sort_dialog_add,
            R.string.library_sort_dialog_abc,
            R.string.library_sort_dialog_pop
        ).map { stringResource(id = it) }
    )
}

@Composable
private fun ChangeReverseSort(isReverseSort: Boolean, sendEvent: (CategoryEvent) -> Unit) {
    CheckBoxText(
        state = isReverseSort,
        onChange = { sendEvent(CategoryEvent.Update(newReverseSort = it)) },
        firstTextId = R.string.library_sort_dialog_reverse
    )
}

@Composable
private fun ChangeVisibility(isVisible: Boolean, sendEvent: (CategoryEvent) -> Unit) {
    CheckBoxText(
        state = isVisible.not(),
        onChange = { sendEvent(CategoryEvent.Update(newVisible = it.not())) },
        firstTextId = R.string.library_sort_dialog_visible
    )
}

@Composable
private fun ColumnScope.ChangePortraitOptions(isLarge: Boolean, span: Int, sendEvent: (CategoryEvent) -> Unit) {
    CheckBoxText(
        state = isLarge,
        onChange = { sendEvent(CategoryEvent.Update(newLargePortrait = it)) },
        firstTextId = R.string.category_dialog_large_cells,
        secondTextId = R.string.category_dialog_small_cells
    )

    SmallSpacer()

    AnimatedVisibility(isLarge) {
        TextWithSlider(R.string.category_dialog_span_text, span, 5) {
            sendEvent(CategoryEvent.Update(newSpanPortrait = it))
        }
    }
}

@Composable
private fun ColumnScope.ChangeLandscapeOptions(
    isLarge: Boolean,
    span: Int,
    sendEvent: (CategoryEvent) -> Unit
) {
    CheckBoxText(
        state = isLarge,
        onChange = { sendEvent(CategoryEvent.Update(newLargeLandscape = it)) },
        firstTextId = R.string.category_dialog_large_cells,
        secondTextId = R.string.category_dialog_small_cells
    )

    SmallSpacer()

    AnimatedVisibility(visible = isLarge) {
        TextWithSlider(R.string.category_dialog_span_text, span, 7) {
            sendEvent(CategoryEvent.Update(newSpanLandscape = it))
        }
    }
}

@Composable
private fun TextWithSlider(
    @StringRes textId: Int,
    state: Int,
    maxPosition: Int,
    onChange: (Int) -> Unit,
) {
    Text(stringResource(textId, state))

    Slider(
        value = state.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = 1f..maxPosition.toFloat(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimensions.default)
    )
}

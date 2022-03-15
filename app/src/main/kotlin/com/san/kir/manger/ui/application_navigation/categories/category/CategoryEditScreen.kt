package com.san.kir.manger.ui.application_navigation.categories.category

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose_utils.CheckBoxText
import com.san.kir.core.compose_utils.DefaultSpacer
import com.san.kir.core.compose_utils.SmallSpacer
import com.san.kir.core.compose_utils.ScreenContent
import com.san.kir.core.compose_utils.topBar
import com.san.kir.manger.R
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.compose.RadioGroup
import kotlin.math.roundToInt

@Composable
// Редактирование параметров категории
fun CategoryEditScreen(
    navigateUp: () -> Unit,
    viewModel: CategoryEditViewModel,
) {
    val viewState by viewModel.state.collectAsState()

    var hasError by rememberSaveable { mutableStateOf(false) }
    var deleteDialog by rememberSaveable { mutableStateOf(false) }

    ScreenContent(
        topBar = topBar(
            title = stringResource(
                if (viewState.hasCreatedNew) R.string.category_dialog_title_create
                else R.string.category_dialog_title_edit
            ),
            navigationListener = navigateUp,
            actions = {
                if (viewState.hasChanges and hasError.not())
                    MenuIcon(
                        icon =
                        if (viewState.hasCreatedNew) Icons.Default.Create
                        else Icons.Default.Save,
                    ) {
                        viewModel.save()
                        viewModel.nullChanges()
                    }
                // Удаление категории полностью
                if (viewState.hasAll.not())
                    MenuIcon(icon = Icons.Default.Delete) {
                        deleteDialog = true
                    }
            }
        )
    ) {
        TextWithValidate(viewModel, hasError) { hasError = it }

        DefaultSpacer()

        Text(stringResource(R.string.category_dialog_sort))

        ChangeSortType(viewModel)

        DefaultSpacer()

        ChangeReverseSort(viewModel = viewModel)

        DefaultSpacer()

        Text(stringResource(R.string.category_dialog_visible))

        ChangeVisibility(viewModel)

        DefaultSpacer()

        Text(text = stringResource(id = R.string.category_dialog_portrait))

        SmallSpacer()

        ChangePortraitOptions(viewModel)

        DefaultSpacer()

        Text(text = stringResource(id = R.string.category_dialog_landscape))

        SmallSpacer()

        ChangeLandscapeOptions(viewModel)
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            text = { Text(text = stringResource(id = R.string.category_item_question_delete)) },
            confirmButton = {
                OutlinedButton(onClick = {
                    viewModel.delete()
                    navigateUp()
                }) {
                    Text(text = stringResource(id = R.string.category_item_question_delete_yes))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    deleteDialog = false
                }) {
                    Text(text = stringResource(id = R.string.category_item_question_delete_no))
                }
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TextWithValidate(
    viewModel: CategoryEditViewModel,
    hasError: Boolean,
    changeHasError: (Boolean) -> Unit,
) {
    val viewState by viewModel.state.collectAsState()

    val tooShortString = stringResource(id = R.string.category_dialog_validate_length)
    val nameIsBusyString = stringResource(id = R.string.category_dialog_validate_contain)

    var validate by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf(viewState.category.name) }

    viewModel.update { name = text }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            changeHasError(true)
            validate = when {
                it.length < 3 -> tooShortString
                viewState.categoryNames.contains(it) -> nameIsBusyString
                else -> {
                    changeHasError(false)
                    ""
                }
            }
            viewModel.newChanges()
        },
        enabled = viewState.hasAll.not(),
        placeholder = { Text(stringResource(id = R.string.category_dialog_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        isError = hasError
    )
    AnimatedVisibility(visible = hasError && viewState.hasAll.not()) {
        Text(
            text = validate,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Red,
        )
    }
}

@Composable
private fun ChangeSortType(
    viewModel: CategoryEditViewModel,
) {
    val viewState by viewModel.state.collectAsState()
    var state by rememberSaveable { mutableStateOf(viewState.category.typeSort) }
    viewModel.update { typeSort = state }

    RadioGroup(
        state,
        onSelected = {
            state = it
            viewModel.newChanges()
        },
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
private fun ChangeReverseSort(
    viewModel: CategoryEditViewModel,
) {
    val viewState by viewModel.state.collectAsState()

    var state by rememberSaveable { mutableStateOf(viewState.category.isReverseSort) }
    viewModel.update { isReverseSort = state }

    CheckBoxText(
        state = state,
        onChange = {
            viewModel.newChanges()
            state = it
        },
        firstTextId = R.string.library_sort_dialog_reverse
    )
}

@Composable
private fun ChangeVisibility(
    viewModel: CategoryEditViewModel,
) {
    val viewState by viewModel.state.collectAsState()

    var state by rememberSaveable { mutableStateOf(viewState.category.isVisible) }
    viewModel.update { isVisible = state }

    CheckBoxText(
        state = state.not(),
        onChange = {
            viewModel.newChanges()
            state = it.not()
        },
        firstTextId = R.string.library_sort_dialog_visible
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ChangePortraitOptions(
    viewModel: CategoryEditViewModel,
) {
    val viewState by viewModel.state.collectAsState()

    var isLarge by rememberSaveable { mutableStateOf(viewState.category.isLargePortrait) }
    viewModel.update { isLargePortrait = isLarge }

    var span by rememberSaveable { mutableStateOf(viewState.category.spanPortrait) }
    viewModel.update { spanPortrait = span }

    CheckBoxText(
        state = isLarge,
        onChange = {
            viewModel.newChanges()
            isLarge = it
        },
        firstTextId = R.string.category_dialog_large_cells,
        secondTextId = R.string.category_dialog_small_cells
    )

    SmallSpacer()

    AnimatedVisibility(isLarge) {
        TextWithSlider(R.string.category_dialog_span_text, span, 5) {
            viewModel.newChanges()
            span = it
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ChangeLandscapeOptions(viewModel: CategoryEditViewModel) {
    val viewState by viewModel.state.collectAsState()

    var isLarge by rememberSaveable { mutableStateOf(viewState.category.isLargeLandscape) }
    viewModel.update { isLargeLandscape = isLarge }

    var span by rememberSaveable { mutableStateOf(viewState.category.spanLandscape) }
    viewModel.update { spanLandscape = span }

    CheckBoxText(
        state = isLarge,
        onChange = {
            viewModel.newChanges()
            isLarge = it
        },
        firstTextId = R.string.category_dialog_large_cells,
        secondTextId = R.string.category_dialog_small_cells
    )

    SmallSpacer()

    AnimatedVisibility(visible = isLarge) {
        TextWithSlider(R.string.category_dialog_span_text, span, 7) {
            viewModel.newChanges()
            span = it
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
    Text(text = stringResource(id = textId, state))

    Slider(
        value = state.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = 1f..maxPosition.toFloat(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    )
}

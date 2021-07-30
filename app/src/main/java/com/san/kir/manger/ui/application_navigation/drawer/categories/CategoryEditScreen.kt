package com.san.kir.manger.ui.application_navigation.drawer.categories

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Checkbox
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.ui.application_navigation.EditCategoryNavigationDestination
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.RadioGroup
import com.san.kir.manger.ui.utils.TopBarScreenWithInsets
import com.san.kir.manger.ui.utils.getElement
import com.san.kir.manger.utils.SortLibraryUtil
import kotlin.math.roundToInt

@ExperimentalAnimationApi
@Composable
fun CategoryEditScreen(
    nav: NavHostController,
    viewModel: CategoryEditViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsState()

    viewModel.setCategory(nav.getElement(EditCategoryNavigationDestination))

    var hasError by rememberSaveable { mutableStateOf(false) }
    var deleteDialog by rememberSaveable { mutableStateOf(false) }

    TopBarScreenWithInsets(
        nav = nav,
        title = stringResource(
            if (viewState.hasCreatedNew) R.string.category_dialog_title_create
            else R.string.category_dialog_title_edit
        ),
        actions = {
            MenuIcon(
                icon =
                if (viewState.hasCreatedNew) Icons.Default.Create
                else Icons.Default.Save,
                enabled = viewState.hasChanges and hasError.not()
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
    ) {
        TextWithValidate(viewModel, hasError) { hasError = it }

        SpacerMain()

        ChangeSortType(viewModel)

        SpacerMain()

        ChangeReverseSort(viewModel = viewModel)

        SpacerMain()

        ChangeVisibility(viewModel)

        SpacerMain()

        Text(text = stringResource(id = R.string.category_dialog_portrait))

        SpacerChild()

        ChangePortraitOptions(viewModel)

        SpacerMain()

        Text(text = stringResource(id = R.string.category_dialog_landscape))

        SpacerChild()

        ChangeLandscapeOptions(viewModel)
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            text = { Text(text = stringResource(id = R.string.category_item_question_delete)) },
            confirmButton = {
                OutlinedButton(onClick = {
                    viewModel.delete()
                    nav.navigateUp()
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

@ExperimentalAnimationApi
@Composable
private fun TextWithValidate(
    viewModel: CategoryEditViewModel,
    hasError: Boolean,
    changeHasError: (Boolean) -> Unit
) {
    val viewState by viewModel.state.collectAsState()

    val tooShortString = stringResource(id = R.string.category_dialog_validate_length)
    val nameIsBusyString = stringResource(id = R.string.category_dialog_validate_contain)

    var validate by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf(viewState.category.name) }

    viewModel.setCategoryProperty(name = text)

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
    viewModel.setCategoryProperty(typeSort = state)

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
    viewModel: CategoryEditViewModel
) {
    val viewState by viewModel.state.collectAsState()

    var state by rememberSaveable { mutableStateOf(viewState.category.isReverseSort) }
    viewModel.setCategoryProperty(isReverseSort = state)

    CheckBoxItem(
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
    viewModel: CategoryEditViewModel
) {
    val viewState by viewModel.state.collectAsState()

    var state by rememberSaveable { mutableStateOf(viewState.category.isVisible) }
    viewModel.setCategoryProperty(isVisible = state)

    CheckBoxItem(
        state = state.not(),
        onChange = {
            viewModel.newChanges()
            state = it.not()
        },
        firstTextId = R.string.library_sort_dialog_visible
    )
}

@ExperimentalAnimationApi
@Composable
private fun ChangePortraitOptions(
    viewModel: CategoryEditViewModel
) {
    val viewState by viewModel.state.collectAsState()

    var isLarge by rememberSaveable { mutableStateOf(viewState.category.isLargePortrait) }
    viewModel.setCategoryProperty(isLargePortrait = isLarge)

    var span by rememberSaveable { mutableStateOf(viewState.category.spanPortrait) }
    viewModel.setCategoryProperty(spanPortrait = span)

    CheckBoxItem(
        state = isLarge,
        onChange = {
            viewModel.newChanges()
            isLarge = it
        },
        firstTextId = R.string.category_dialog_large_cells,
        secondTextId = R.string.category_dialog_small_cells
    )

    SpacerChild()

    AnimatedVisibility(isLarge) {
        TextWithSlider(R.string.category_dialog_span_text, span, 5) {
            viewModel.newChanges()
            span = it
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun ChangeLandscapeOptions(viewModel: CategoryEditViewModel) {
    val viewState by viewModel.state.collectAsState()

    var isLarge by rememberSaveable { mutableStateOf(viewState.category.isLargeLandscape) }
    viewModel.setCategoryProperty(isLargeLandscape = isLarge)

    var span by rememberSaveable { mutableStateOf(viewState.category.spanLandscape) }
    viewModel.setCategoryProperty(spanLandscape = span)

    CheckBoxItem(
        state = isLarge,
        onChange = {
            viewModel.newChanges()
            isLarge = it
        },
        firstTextId = R.string.category_dialog_large_cells,
        secondTextId = R.string.category_dialog_small_cells
    )

    SpacerChild()

    AnimatedVisibility(visible = isLarge) {
        TextWithSlider(R.string.category_dialog_span_text, span, 7) {
            viewModel.newChanges()
            span = it
        }
    }
}

@Composable
private fun CheckBoxItem(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    @StringRes firstTextId: Int,
    @StringRes secondTextId: Int = -1,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onChange(state.not()) }) {

        Checkbox(
            checked = state,
            onCheckedChange = onChange,
            modifier = Modifier.padding(end = 10.dp)
        )

        if (secondTextId == -1)
            Text(text = stringResource(id = firstTextId))
        else
            Text(
                text = stringResource(
                    id = if (state)
                        firstTextId
                    else
                        secondTextId
                )
            )
    }
}

@Composable
private fun TextWithSlider(
    @StringRes textId: Int,
    state: Int,
    maxPosition: Int,
    onChange: (Int) -> Unit
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

@Composable
private fun SpacerChild() {
    val spaceHeightChild = 8.dp
    Spacer(modifier = Modifier.height(spaceHeightChild))
}

@Composable
private fun SpacerMain() {
    val spaceHeightMain = 16.dp
    Spacer(modifier = Modifier.height(spaceHeightMain))
}

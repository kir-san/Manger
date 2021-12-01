package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.MangaUpdaterService
import com.san.kir.manger.utils.compose.DialogText
import com.san.kir.manger.utils.compose.TopBarScreenWithInsets
import kotlinx.coroutines.delay

@Composable
fun MangaAddScreen(url: String, navigateUpAction: () -> Unit) {
    TopBarScreenWithInsets(
        navigationButtonListener = navigateUpAction,
        title = stringResource(id = R.string.add_manga_screen_title)
    ) {
        MangaAddContent(url, navigateUpAction)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ColumnScope.MangaAddContent(
    url: String,
    closeBtnAction: () -> Unit,
    viewModel: MangaAddViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val (close, closeSetter) = remember { mutableStateOf(false) }

    var continueProcess by remember { mutableStateOf(false) }
    var continueBtn by remember { mutableStateOf(true) }

    TextWithValidate(state.inputText, viewModel::changeText)

    MessageAboutCreatingNewCategory(state.newChapter)

    ListOfAvailableCategories(state.validateCategories, viewModel::changeText)

    if (continueProcess) ContinueProcess(url, state.inputText, closeSetter)

    Spacer(modifier = Modifier.weight(1f, true))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedVisibility(visible = continueBtn) {
            Button(
                onClick = {
                    continueProcess = true
                    continueBtn = false
                },
                enabled = state.activateContinue
            ) {
                Text(text = stringResource(id = R.string.add_manga_screen_continue))
            }
        }

        AnimatedVisibility(visible = close) {
            Button(closeBtnAction) {
                Text(text = stringResource(id = R.string.add_manga_close_btn))
            }
        }
    }
}

@Composable
private fun TextWithValidate(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 3.dp),
        placeholder = { Text(stringResource(id = R.string.add_manga_screen_item)) },
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MessageAboutCreatingNewCategory(visible: Boolean) {
    AnimatedVisibility(visible) {
        Text(
            stringResource(id = R.string.add_manga_screen_add_new),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.error,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ListOfAvailableCategories(
    listOfCategories: List<String>,
    onItemSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        mainAxisAlignment = FlowMainAxisAlignment.End,
        crossAxisAlignment = FlowCrossAxisAlignment.End,
    ) {
        listOfCategories.forEach { item ->
            Card(modifier = Modifier
                .padding(5.dp)
                .clickable { onItemSelect(item) }) {
                Text(item, modifier = Modifier.padding(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ContinueProcess(
    url: String,
    category: String,
    onEndProcess: (Boolean) -> Unit,
    viewModel: MangaAddViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    var action by remember { mutableStateOf(true) }
    var process by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf(false) }
    var added by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = added) {
        DialogText(
            text = stringResource(id = R.string.add_manga_screen_created_category, category)
        )
    }
    AnimatedVisibility(visible = process >= ProcessStatus.categoryChanged) {
        DialogText(
            text = stringResource(id = R.string.add_manga_screen_changed_category, category)
        )
    }
    AnimatedVisibility(visible = process >= ProcessStatus.prevAndUpdateManga) {
        DialogText(text = stringResource(id = R.string.add_manga_screen_update_manga))
    }
    AnimatedVisibility(visible = process >= ProcessStatus.prevAndCreatedFolder) {
        DialogText(text = stringResource(id = R.string.add_manga_screen_created_folder))
    }
    AnimatedVisibility(visible = process >= ProcessStatus.prevAndSearchChapters) {
        DialogText(text = stringResource(id = R.string.add_manga_screen_search_chapters))
    }
    AnimatedVisibility(visible = process >= ProcessStatus.allComplete) {
        DialogText(text = stringResource(id = R.string.add_manga_screen_all_complete))
    }
    AnimatedVisibility(visible = error) {
        DialogText(text = stringResource(id = R.string.add_manga_screen_error))
    }

    if (action)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .padding(vertical = 5.dp)
        )

    LaunchedEffect(true) {
        kotlin.runCatching {
            error = false
            onEndProcess(false)

            if (!viewModel.hasCategory(category)) {
                added = true
                viewModel.addCategory(category)
                delay(1000)
            }
            process = ProcessStatus.categoryChanged

            process = ProcessStatus.prevAndUpdateManga
            if (url.isNotEmpty()) {

                val (path, manga) = viewModel.updateSiteElement(url, category)!!

                process = ProcessStatus.prevAndCreatedFolder
                viewModel.createDirs(path)
                delay(1000)

                process = ProcessStatus.prevAndSearchChapters
                MangaUpdaterService.add(context, manga)
                delay(1000)

                process = ProcessStatus.allComplete

                action = false
                onEndProcess(true)
            }
        }.fold(
            onSuccess = {
                error = false
            },
            onFailure = {
                error = true
                it.printStackTrace()
                action = false
                onEndProcess(true)
            }
        )
    }
}

private object ProcessStatus {
    const val categoryChanged = 1
    const val prevAndUpdateManga = 2
    const val prevAndCreatedFolder = 3
    const val prevAndSearchChapters = 4
    const val allComplete = 5
}

@Preview(
    name = "PreviewListOfAvailableCategories Light",
    group = "ListOfAvailableCategories",
    showBackground = true,
)
@Composable
fun PreviewListOfAvailableCategoriesLight() {
    MaterialTheme(colors = lightColors()) {
        ListOfAvailableCategories(listOfCategories = listOf("Test 1", "Test 2", "Test 3"),
            onItemSelect = {})
    }
}

@Preview(
    name = "PreviewListOfAvailableCategories Dark",
    group = "ListOfAvailableCategories",
)
@Composable
fun PreviewListOfAvailableCategoriesDark() {
    MaterialTheme(colors = darkColors()) {
        ListOfAvailableCategories(listOfCategories = listOf("Test 1", "Test 2", "Test 3"),
            onItemSelect = {})
    }
}

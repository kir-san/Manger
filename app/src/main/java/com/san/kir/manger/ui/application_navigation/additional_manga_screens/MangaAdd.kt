package com.san.kir.manger.ui.application_navigation.additional_manga_screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.san.kir.ankofork.startService
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.ui.utils.DialogText
import com.san.kir.manger.ui.utils.TopBarScreenWithInsets
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun MangaAddScreen(nav: NavHostController, item: SiteCatalogElement) {
    TopBarScreenWithInsets(
        navigationButtonListener = { nav.navigateUp() },
        title = stringResource(id = R.string.add_manga_screen_title)
    ) {
        MangaAddContent(item, nav)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ColumnScope.MangaAddContent(
    item: SiteCatalogElement,
    nav: NavHostController,
    viewModel: MangaAddViewModel = hiltViewModel()
) {

    var categories by remember { mutableStateOf(emptyList<String>()) }
    var validate by remember { mutableStateOf(categories) }

    LaunchedEffect(true) {
        categories = viewModel.getCategories()
        validate = categories
    }

    val isEnable = remember { mutableStateOf(false) }
    val isNew = remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    var continueProcess by remember { mutableStateOf(false) }
    var continueBtn by remember { mutableStateOf(true) }
    val closeBtn = remember { mutableStateOf(false) }

    TextField(
        value = inputText,
        onValueChange = {
            inputText = it
            validate = validate(it, categories, isEnable, isNew)
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 3.dp),
        placeholder = { Text(stringResource(id = R.string.add_manga_screen_item)) },
    )

    AnimatedVisibility(visible = isNew.value) {
        Text(
            stringResource(id = R.string.add_manga_screen_add_new),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.error,
            modifier = Modifier.fillMaxWidth()
        )
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        mainAxisAlignment = FlowMainAxisAlignment.End,
        crossAxisAlignment = FlowCrossAxisAlignment.End,
    ) {
        validate.forEach { item ->
            Card(modifier = Modifier
                .padding(5.dp)
                .clickable {
                    inputText = item
                    validate = validate(item, categories, isEnable, isNew)
                }) {
                Text(item, modifier = Modifier.padding(6.dp))
            }
        }
    }

    if (continueProcess) ContinueProcess(item, inputText, closeBtn)

    Spacer(modifier = Modifier.weight(1f, true))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedVisibility(visible = continueBtn) {
            Button(onClick = {
                continueProcess = true
                continueBtn = false
            }, enabled = isEnable.value) {
                Text(text = stringResource(id = R.string.add_manga_screen_continue))
            }
        }

        AnimatedVisibility(visible = closeBtn.value) {
            Button(onClick = { nav.navigateUp() }) {
                Text(text = stringResource(id = R.string.add_manga_close_btn))
            }
        }
    }
}


private fun validate(
    text: String,
    categories: List<String>,
    isEnable: MutableState<Boolean>,
    isNew: MutableState<Boolean>
): List<String> {

    isEnable.value = text.length >= 3
    return if (text.isNotBlank()) {
        // список категорий подходящих под введенное
        val temp = categories.filter { it.contains(text) }

        isNew.value = !(temp.size == 1 && temp.first() == text)

        temp
    } else {
        // Если нет текста, то отображается список
        // доступных сайтов
        isNew.value = true
        categories
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ContinueProcess(
    item: SiteCatalogElement,
    category: String,
    closeBtn: MutableState<Boolean>,
    viewModel: MangaAddViewModel = hiltViewModel()
) {
    val context = LocalContext.current

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

    // TODO проверить причину не загрузки лого
    LaunchedEffect(true) {
        kotlin.runCatching {
            if (!viewModel.hasCategory(category)) {
                added = true
                viewModel.addCategory(category)
                delay(1000)
            }
            process = ProcessStatus.categoryChanged

            process = ProcessStatus.prevAndUpdateManga
            val (path, manga) = viewModel.updateSiteElement(item, category)

            process = ProcessStatus.prevAndCreatedFolder
            viewModel.createDirs(path)
            delay(1000)

            process = ProcessStatus.prevAndSearchChapters
            context.startService<MangaUpdaterService>(MangaColumn.tableName to manga)
            delay(1000)

            process = ProcessStatus.allComplete
        }.fold(
            onSuccess = { },
            onFailure = {
                error = true
                it.printStackTrace()
            }
        )
        action = false
        closeBtn.value = true
    }
}

private object ProcessStatus {
    const val categoryChanged = 1
    const val prevAndUpdateManga = 2
    const val prevAndCreatedFolder = 3
    const val prevAndSearchChapters = 4
    const val allComplete = 5
}

class SiteCatalogItemViewModel @AssistedInject constructor(
    @Assisted private val url: String,
    private val manager: SiteCatalogsManager,
) : ViewModel() {
    private val _item = MutableStateFlow(SiteCatalogElement())
    val item = _item.asStateFlow()

    init {
        // инициация манги
        viewModelScope.launch(Dispatchers.Default) {
            manager.getElementOnline(url)?.also { _item.update { it } }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(url: String): SiteCatalogItemViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            url: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(url) as T
            }
        }
    }
}

@Composable
fun siteCatalogItemViewModel(url: String): SiteCatalogItemViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).siteCatalogItemViewModelFactory()

    return viewModel(factory = SiteCatalogItemViewModel.provideFactory(factory, url))
}

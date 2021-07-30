package com.san.kir.manger.ui.application_navigation.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.ui.application_navigation.CatalogNavigationDestination
import com.san.kir.manger.ui.LocalBaseViewModel
import com.san.kir.manger.ui.application_navigation.catalog.CatalogViewModel.Companion.DATE
import com.san.kir.manger.ui.application_navigation.catalog.CatalogViewModel.Companion.NAME
import com.san.kir.manger.ui.application_navigation.catalog.CatalogViewModel.Companion.POP
import com.san.kir.manger.ui.utils.ListItem
import com.san.kir.manger.ui.utils.MenuIcon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.net.URLDecoder

val btnSizeAddUpdate = 30.dp

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun CatalogScreen(
    nav: NavHostController,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val vm = LocalBaseViewModel.current
    val site =
        ManageSites.CATALOG_SITES.first {
            it.name == nav.currentBackStackEntry?.arguments?.getString(CatalogNavigationDestination().siteName)
        }

    val viewState by viewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    viewModel.setSite(site.catalogName)

    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val coroutineScope = rememberCoroutineScope()
    val errorDialog = remember { mutableStateOf(false) }
    val reloadDialog = remember { mutableStateOf(false) }

    when (vm.catalogReceiver.collectAsState().value) {
        "destroy" -> viewModel.setAction(false)
        "error" -> errorDialog.value = true
    }

    Scaffold(
        modifier = Modifier.navigationBarsWithImePadding(),
        topBar = { TopBar(scaffoldState, viewState, site, viewModel) },
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(viewState, viewModel) },
        bottomBar = { BottomBar(viewModel, reloadDialog) }) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (action) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn {
                items(items = viewState.items, key = { item -> item.id }) { item ->
                    ListItem(item, item.name, item.statusEdition, nav)
                }
            }
        }
    }

    BackHandler {
        if (scaffoldState.drawerState.isOpen) {
            coroutineScope.launch {
                scaffoldState.drawerState.close()
            }
        } else {
            nav.popBackStack()
        }
    }

    ReloadDialog(reloadDialog, viewModel)
    ErrorReloadDialog(errorDialog, viewModel)
}

// Оповещение о произошедшей ошибке во время обновления каталога
@Composable
private fun ErrorReloadDialog(
    errorDialog: MutableState<Boolean>,
    viewModel: CatalogViewModel
) {
    var error by errorDialog
    if (error) {
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text(text = stringResource(id = R.string.manga_error_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.manga_error_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAction(false)
                    error = false
                }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}

// Диалог подтверждения на обновление каталога
@Composable
private fun ReloadDialog(
    reloadDialog: MutableState<Boolean>,
    viewModel: CatalogViewModel
) {
    var reload by reloadDialog
    if (reload) {
        AlertDialog(onDismissRequest = { reload = false },
                    title = { Text(text = stringResource(id = R.string.catalog_fot_one_site_warning)) },
                    text = { Text(text = stringResource(id = R.string.catalog_fot_one_site_redownload_text)) },
                    confirmButton = {
                        TextButton(onClick = {
                            reload = false
                            viewModel.setAction(value = true, service = true)
                        }) {
                            Text(text = stringResource(id = R.string.catalog_fot_one_site_redownload_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { reload = false }) {
                            Text(text = stringResource(id = R.string.catalog_fot_one_site_redownload_cancel))
                        }
                    })
    }
}

// Нижняя панель с кнопками сортировки списка
@Composable
private fun BottomBar(
    viewModel: CatalogViewModel,
    reloadDialog: MutableState<Boolean>,
) {
    var reverse by rememberSaveable { mutableStateOf(false) }
    viewModel.setIsReversed(reverse)

    val sortType = rememberSaveable { mutableStateOf(DATE) }
    viewModel.setSortType(sortType.value)

    BottomAppBar {
        IconToggleButton(
            checked = reverse,
            onCheckedChange = { reverse = it }) {
            Image(
                Icons.Default.Sort, "",
                Modifier
                    .padding(start = 16.dp)
                    .size(btnSizeAddUpdate)
                    .rotate(if (reverse) 0f else 180f),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
        }
        Row(
            modifier = Modifier.weight(1f, true),
            horizontalArrangement = Arrangement.Center
        ) {
            MiddleBottomBtn(sortType, NAME, Icons.Default.SortByAlpha)
            MiddleBottomBtn(sortType, DATE, Icons.Default.DateRange)
            MiddleBottomBtn(sortType, POP, Icons.Default.ThumbsUpDown)
        }
        IconButton(onClick = { reloadDialog.value = true }) {
            Image(
                Icons.Default.Update, "",
                Modifier
                    .padding(end = 16.dp)
                    .size(btnSizeAddUpdate),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
        }
    }
}

// Верхняя панель
@ExperimentalAnimationApi
@Composable
private fun TopBar(
    scaffoldState: ScaffoldState,
    viewState: CatalogViewState,
    site: SiteCatalog,
    viewModel: CatalogViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var search by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    viewModel.setSearchText(searchText)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(text = "${site.name}: ${viewState.items.size}")
            },
            navigationIcon = {
                MenuIcon(icon = Icons.Default.Menu) {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
            },
            actions = {
                MenuIcon(icon = Icons.Default.Search) {
                    search = !search
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        )

        AnimatedVisibility(visible = search) {
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                leadingIcon = { Icon(Icons.Default.Search, "search") },
                trailingIcon = {
                    MenuIcon(icon = Icons.Default.Close) {
                        searchText = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            )
        }
    }
}

// Боковое меню
@Composable
private fun DrawerContent(viewState: CatalogViewState, viewModel: CatalogViewModel) {
    if (viewState.filters.isNotEmpty()) {
        var currentIndex by rememberSaveable { mutableStateOf(0) }
        var filters by rememberSaveable {
            mutableStateOf(viewState.filters.map { it.selected })
        }

        // TODO исправить некоректную работу фильтров
        // Списки фильтров
        Column(
            modifier = Modifier
                .statusBarsPadding()
        ) {
            Crossfade(
                targetState = currentIndex,
                modifier = Modifier.weight(1f, true)
            ) { pageIndex ->
                LazyColumn {
                    itemsIndexed(
                        viewState.filters[pageIndex].catalog,
                        key = { _, item -> item }
                    ) { index, item ->
                        // Строка списка
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    filters[pageIndex][index].value =
                                        !filters[pageIndex][index].value
                                    // Обработка измения статуса
                                    viewModel.changeFilter(
                                        // Хранение статуса нажатия
                                        pageIndex, filters[pageIndex][index].value, item
                                    )
                                }) {
                            Checkbox(
                                checked = filters[pageIndex][index].value,
                                onCheckedChange = { filters[pageIndex][index].value = it },
                                modifier = Modifier.padding(5.dp),
                            )
                            Text(
                                text = URLDecoder.decode(item, "UTF-8"),
                                modifier = Modifier.padding(5.dp),
                            )
                        }
                    }
                }
            }

            // Горизонтальная линия
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RectangleShape)
                    .background(MaterialTheme.colors.onBackground)
            )

            // Переключатели вкладок доступных фильтров
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                viewState.filters.forEachIndexed { index, catalogFilter ->
                    if (catalogFilter.catalog.size > 1)
                        Box(
                            modifier = Modifier
                                .toggleable(
                                    value = currentIndex == index,
                                    onValueChange = { currentIndex = index },
                                    role = Role.Checkbox,
                                )
                                .fillMaxSize()
                                .weight(1f, true),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = catalogFilter.name,
                                modifier = Modifier.padding(6.dp),
                                color = if (currentIndex == index)
                                    MaterialTheme.colors.primary
                                else
                                    MaterialTheme.colors.onBackground,
                                textAlign = TextAlign.Center,
                            )
                        }
                }
            }

            // Кнопка очистки фильтров
            Button(
                onClick = {
                    filters = filters.onEach { it.onEach { it.value = false } }
                    viewModel.clearSelected()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(text = "Очистить")
            }
        }
    }
}

// Default radius of an unbounded ripple in an IconButton
//private val RippleRadius = 24.dp

// Кнопка нижней панели
@Composable
private fun MiddleBottomBtn(sortType: MutableState<Int>, state: Int, icon: ImageVector) {
    IconToggleButton(
        checked = sortType.value == state,
        onCheckedChange = { if (it) sortType.value = state }) {
        Image(
            icon, "", Modifier.size(btnSizeAddUpdate),
            colorFilter = ColorFilter.tint(
                if (sortType.value == state) Color.Cyan else MaterialTheme.colors.onSurface
            )
        )
    }
}


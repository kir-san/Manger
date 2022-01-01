package com.san.kir.manger.ui.application_navigation.catalog.catalog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import com.san.kir.core.utils.log
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogViewModel.Companion.DATE
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogViewModel.Companion.NAME
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogViewModel.Companion.POP
import com.san.kir.manger.utils.compose.ListItem
import com.san.kir.ui.utils.MenuIcon
import com.san.kir.manger.utils.compose.navigate
import kotlinx.coroutines.launch
import java.net.URLDecoder

val btnSizeAddUpdate = 30.dp

@Composable
fun CatalogScreen(nav: NavHostController, viewModel: CatalogViewModel) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val coroutineScope = rememberCoroutineScope()
    var errorDialog by remember { mutableStateOf(false) }
    val items by viewModel.items.collectAsState()
    val action by viewModel.action.collectAsState()

    Scaffold(
        modifier = Modifier.navigationBarsWithImePadding(),
        topBar = { TopBar(scaffoldState, viewModel) },
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(viewModel) },
        bottomBar = { BottomBar(viewModel) }) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (action) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn {
                items(items = items, key = { item -> item.id }) { item ->
                    ListItem(item, item.name, item.statusEdition,
                        navAddAction = {
                            nav.navigate(CatalogsNavTarget.AddLocal, item.link)
                        },
                        navInfoAction = {
                            nav.navigate(CatalogsNavTarget.Info, item.link)
                        })
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
            nav.navigateUp()
        }
    }

    if (errorDialog)
        ErrorReloadDialog(viewModel) { errorDialog = false }

    if (CatalogForOneSiteUpdaterService.isContain(viewModel.siteName)) viewModel.setAction(true)

    ReceiverHandler(
        viewModel.siteName,
        action = {
            viewModel.setAction(it)
            if (it.not()) viewModel.update()
        },
        error = { errorDialog = true })
}

@Composable
private fun ReceiverHandler(
    siteName: String,
    action: (Boolean) -> Unit,
    error: () -> Unit,
) {
    val context = LocalContext.current

    val currentAction by rememberUpdatedState(action)
    val currentError by rememberUpdatedState(error)

    DisposableEffect(context) {
        val intentFilter =
            IntentFilter(CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE)

        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null
                    && intent.action == CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE
                ) {
                    log("onReceiver $intent")
                    intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT)
                        ?.let { out ->
                            when (out) {
                                "destroy" -> currentAction(false)
                                "error" -> currentError()
                                siteName -> currentAction(false)
                                else -> currentAction(true)
                            }
                        }

                }
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}

// Оповещение о произошедшей ошибке во время обновления каталога
@Composable
private fun ErrorReloadDialog(
    viewModel: CatalogViewModel,
    onChange: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onChange() },
        title = { Text(text = stringResource(id = R.string.manga_error_dialog_title)) },
        text = { Text(text = stringResource(id = R.string.manga_error_dialog_message)) },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setAction(false)
                onChange()
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}

// Нижняя панель с кнопками сортировки списка
@Composable
private fun BottomBar(viewModel: CatalogViewModel) {
    var reloadDialog by remember { mutableStateOf(false) }

    BottomAppBar {
        IconToggleButton(
            checked = viewModel.isReversed,
            onCheckedChange = { viewModel.isReversed = it }) {
            Image(
                Icons.Default.Sort, "",
                Modifier
                    .padding(start = 16.dp)
                    .size(btnSizeAddUpdate)
                    .rotate(if (viewModel.isReversed) 0f else 180f),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
        }
        Row(
            modifier = Modifier.weight(1f, true),
            horizontalArrangement = Arrangement.Center
        ) {
            MiddleBottomBtn(
                viewModel.sortType == NAME,
                { viewModel.sortType = NAME },
                Icons.Default.SortByAlpha
            )
            MiddleBottomBtn(
                viewModel.sortType == DATE,
                { viewModel.sortType = DATE },
                Icons.Default.DateRange
            )
            MiddleBottomBtn(
                viewModel.sortType == POP,
                { viewModel.sortType = POP },
                Icons.Default.ThumbsUpDown
            )
        }
        IconButton(onClick = { reloadDialog = true }) {
            Image(
                Icons.Default.Update, "",
                Modifier
                    .padding(end = 16.dp)
                    .size(btnSizeAddUpdate),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
        }
    }

    if (reloadDialog)
        ReloadDialog(viewModel) { reloadDialog = false }
}

// Диалог подтверждения на обновление каталога
@Composable
private fun ReloadDialog(
    viewModel: CatalogViewModel,
    onChange: () -> Unit,
) {
    AlertDialog(onDismissRequest = { onChange() },
        title = { Text(text = stringResource(id = R.string.catalog_fot_one_site_warning)) },
        text = { Text(text = stringResource(id = R.string.catalog_fot_one_site_redownload_text)) },
        confirmButton = {
            TextButton(onClick = {
                onChange()
                viewModel.setAction(value = true, service = true)
            }) {
                Text(text = stringResource(id = R.string.catalog_fot_one_site_redownload_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onChange() }) {
                Text(text = stringResource(id = R.string.catalog_fot_one_site_redownload_cancel))
            }
        })
}

// Верхняя панель
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TopBar(
    scaffoldState: ScaffoldState,
    viewModel: CatalogViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    var search by rememberSaveable { mutableStateOf(false) }
    val items by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(text = "${viewModel.siteName}: ${items.size}")
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
                value = viewModel.searchText,
                onValueChange = {
                    viewModel.searchText = it
                },
                leadingIcon = { Icon(Icons.Default.Search, "search") },
                trailingIcon = {
                    MenuIcon(icon = Icons.Default.Close) {
                        viewModel.searchText = ""
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
private fun DrawerContent(viewModel: CatalogViewModel) {
    val filters by viewModel.filters.collectAsState()

    if (filters.isNotEmpty()) {
        var currentIndex by rememberSaveable { mutableStateOf(0) }

        // Списки фильтров
        Column(
            modifier = Modifier
                .statusBarsPadding()
        ) {
            Crossfade(
                targetState = currentIndex,
                modifier = Modifier.weight(1f, true)
            ) { pageIndex ->
                val currentFilter = filters[pageIndex]

                LazyColumn {
                    itemsIndexed(currentFilter.catalog, key = { _, item -> item }) { index, item ->
                        if (currentFilter.selected[index]) {
                            viewModel.selectedNames += SelectedName(currentFilter.name, item)
                        } else {
                            viewModel.selectedNames -= SelectedName(currentFilter.name, item)
                        }

                        // Строка списка
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentFilter.selected[index] = !currentFilter.selected[index]
                                }) {
                            Checkbox(
                                checked = currentFilter.selected[index],
                                onCheckedChange = { currentFilter.selected[index] = it },
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
                filters.forEachIndexed { index, catalogFilter ->
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

// Кнопка нижней панели
@Composable
private fun MiddleBottomBtn(state: Boolean, onChange: () -> Unit, icon: ImageVector) {
    IconToggleButton(
        checked = state,
        onCheckedChange = { if (it) onChange() }) {
        Image(
            icon, "", Modifier.size(btnSizeAddUpdate),
            colorFilter = ColorFilter.tint(
                if (state) Color.Cyan else MaterialTheme.colors.onSurface
            )
        )
    }
}


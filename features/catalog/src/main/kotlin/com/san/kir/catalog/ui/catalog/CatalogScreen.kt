package com.san.kir.catalog.ui.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.catalog.R
import com.san.kir.catalog.utils.ListItem
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.startInsetsPadding
import com.san.kir.core.compose.systemBarBottomPadding
import com.san.kir.core.compose.systemBarStartPadding
import com.san.kir.core.compose.systemBarTopPadding
import com.san.kir.core.compose.topBar
import com.san.kir.data.models.extend.MiniCatalogItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.net.URLDecoder

@Composable
fun CatalogScreen(
    navigateUp: () -> Boolean,
    navigateToInfo: (String) -> Unit,
    navigateToAdd: (String) -> Unit,
    catalogName: String,
) {
    val viewModel: CatalogViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val filters by viewModel.filters.collectAsState()

    LaunchedEffect(Unit) { viewModel.sendEvent(CatalogEvent.Set(catalogName)) }

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var enableSearch by rememberSaveable { mutableStateOf(false) }
    val query: ((String) -> Unit)? =
        remember(enableSearch) {
            if (enableSearch) {
                { viewModel.sendEvent(CatalogEvent.Search(it)) }
            } else null
        }
    val update =
        remember { { arg: MiniCatalogItem -> viewModel.sendEvent(CatalogEvent.UpdateManga(arg)) } }

    ScreenList(
        scaffoldState = scaffoldState,
        topBar = topBar(
            title = "${state.title}: ${state.items.size}",
            navigationButton = navigationButtonToggle(
                filters.isNotEmpty(), scaffoldState, navigateUp
            ),
            actions = { MenuIcon(Icons.Default.Search) { enableSearch = !enableSearch } },
            onSearchTextChange = query,
            hasAction = state.background.currentState,
            progressAction = state.background.progress,
        ),
        drawerContent = drawerToogle(filters, viewModel::sendEvent),
        bottomBar = { height ->
            BottomBar(
                sort = state.sort,
                background = state.background,
                sendEvent = viewModel::sendEvent,
                height = height
            )
        },
        additionalPadding = Dimensions.quarter,
//        enableCollapsingBars = true,
    ) {
        items(items = state.items, key = { item -> item.id }) { item ->
            ListItem(
                item, item.statusEdition,
                toAdd = navigateToAdd,
                toInfo = navigateToInfo,
                updateItem = update
            )
        }
    }

    BackHandler {
        if (scaffoldState.drawerState.isOpen) {
            coroutineScope.launch { scaffoldState.drawerState.close() }
        } else {
            navigateUp()
        }
    }
}

// Нижняя панель с кнопками сортировки списка
@Composable
private fun BottomBar(
    sort: SortState,
    background: BackgroundState,
    sendEvent: (CatalogEvent) -> Unit,
    height: Dp = Dimensions.appBarHeight
) {
    var reloadDialog by remember { mutableStateOf(false) }
    var cancelDialog by remember { mutableStateOf(false) }

    BottomAppBar(
        contentPadding =
        if (height == Dimensions.zero) PaddingValues(Dimensions.zero)
        else systemBarBottomPadding(),
        modifier = Modifier.height(height + systemBarBottomPadding().calculateBottomPadding())
    ) {
        IconToggleButton(
            modifier = Modifier.padding(systemBarStartPadding(Dimensions.default)),
            checked = sort.reverse,
            onCheckedChange = { sendEvent(CatalogEvent.Reverse) },
        ) {
            Image(
                Icons.Default.Sort, "",
                Modifier
                    .size(Dimensions.Image.small)
                    .graphicsLayer {
                        rotationZ = if (sort.reverse) 0f else 180f
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
        }
        Row(
            modifier = Modifier.weight(1f, true),
            horizontalArrangement = Arrangement.Center
        ) {
            MiddleBottomBtn(
                sort.type is SortType.Name,
                { sendEvent(CatalogEvent.ChangeSort(SortType.Name)) },
                Icons.Default.SortByAlpha
            )
            MiddleBottomBtn(
                sort.type is SortType.Date,
                { sendEvent(CatalogEvent.ChangeSort(SortType.Date)) },
                Icons.Default.DateRange
            )
            MiddleBottomBtn(
                sort.type is SortType.Pop,
                { sendEvent(CatalogEvent.ChangeSort(SortType.Pop)) },
                Icons.Default.ThumbsUpDown
            )
        }

        FromEndToEndAnimContent(
            targetState = background.updateCatalogs,
            modifier = Modifier
                .padding(Dimensions.default)
                .endInsetsPadding()
        ) {
            when (it) {
                true -> IconButton(onClick = { cancelDialog = true }) {
                    Image(
                        Icons.Default.Close, "",
                        Modifier.size(Dimensions.Image.small),
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                    )
                }

                false -> IconButton(onClick = { reloadDialog = true }) {
                    Image(
                        Icons.Default.Update, "",
                        Modifier.size(Dimensions.Image.small),
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                    )
                }
            }
        }

    }

    if (reloadDialog) ReloadDialog(sendEvent) { reloadDialog = false }
    if (cancelDialog) CancelDialog(sendEvent) { cancelDialog = false }
}

// Диалог подтверждения на обновление каталога
@Composable
private fun ReloadDialog(
    sendEvent: (CatalogEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.catalog_fot_one_site_warning)) },
        text = { Text(stringResource(R.string.update_catalog)) },
        confirmButton = {
            TextButton(onClick = {
                sendEvent(CatalogEvent.UpdateContent)
                onDismiss()
            }) {
                Text(stringResource(R.string.agree))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.not_agree))
            }
        })
}

// Диалог отмены обновления каталога
@Composable
private fun CancelDialog(
    sendEvent: (CatalogEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.catalog_fot_one_site_warning)) },
        text = { Text(stringResource(R.string.cancel_update_catalog)) },
        confirmButton = {
            TextButton(onClick = {
                sendEvent(CatalogEvent.CancelUpdateContent)
                onDismiss()
            }) {
                Text(stringResource(R.string.agree))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.not_agree))
            }
        })
}

private fun navigationButtonToggle(
    state: Boolean,
    scaffoldState: ScaffoldState,
    navigateUp: () -> Boolean
) =
    if (state) NavigationButton.Scaffold(scaffoldState)
    else NavigationButton.Back(navigateUp)

private fun drawerToogle(filters: ImmutableList<Filter>, sendEvent: (CatalogEvent) -> Unit):
        @Composable (ColumnScope.() -> Unit)? =
    if (filters.isNotEmpty()) {
        { DrawerContent(filters = filters, sendEvent = sendEvent) }
    } else null

// Боковое меню
@Composable
private fun DrawerContent(filters: ImmutableList<Filter>, sendEvent: (CatalogEvent) -> Unit) {
    var currentIndex by rememberSaveable { mutableStateOf(0) }

    // Списки фильтров
    Column {
        Crossfade(
            targetState = currentIndex,
            modifier = Modifier.weight(1f, true)
        ) { pageIndex ->
            val currentFilter = filters[pageIndex]

            LazyColumn(contentPadding = systemBarTopPadding()) {
                itemsIndexed(currentFilter.items, key = { i, _ -> i }) { index, item ->
                    // Строка списка
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sendEvent(CatalogEvent.ChangeFilter(currentFilter.type, index))
                            }
                            .startInsetsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.state,
                            onCheckedChange = { },
                        )
                        Text(URLDecoder.decode(item.name, "UTF-8"))
                    }
                }
            }
        }

        // Горизонтальная линия
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.smallest)
                .clip(RectangleShape)
                .background(MaterialTheme.colors.onBackground)
        )

        // Переключатели вкладок доступных фильтров
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .startInsetsPadding()
        ) {
            filters.forEachIndexed { index, catalogFilter ->
                if (catalogFilter.items.size > 1)
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
                            text = stringResource(catalogFilter.type.stringId),
                            modifier = Modifier.padding(Dimensions.smaller),
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
            onClick = { sendEvent(CatalogEvent.ClearFilters) },
            modifier = Modifier
                .fillMaxWidth()
                .startInsetsPadding()
                .bottomInsetsPadding()
        ) {
            Text(stringResource(R.string.clear))
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
            icon, "",
            Modifier.size(Dimensions.Image.small),
            colorFilter = ColorFilter.tint(if (state) Color.Cyan else MaterialTheme.colors.onSurface)
        )
    }
}

package com.san.kir.manger.ui.application_navigation.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.ui.application_navigation.ApplicationNavigationDestination.Drawer
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.view_models.DrawerViewModel
import com.san.kir.manger.view_models.TitleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DrawerScreen(
    close: () -> Unit,
    mainNav: NavHostController,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val navControlller = rememberAnimatedNavController()
    val navBackStackEntry by navControlller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopBar(scaffoldState, currentRoute, mainNav) },
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = true,
        drawerContent = { DrawerContent(viewModel, scaffoldState, currentRoute, navControlller) },
    ) { contentPadding ->
        AnimatedNavHost(
            navController = navControlller,
            graph = navControlller.drawerGraph(mainNav, contentPadding)
        )
    }

    BackHandler {
        coroutineScope.launch {
            if (scaffoldState.drawerState.isOpen) {
                scaffoldState.drawerState.close()
            } else {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    close()
                } else {
                    context.toast(R.string.first_run_exit_text)
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }
}

var backPressedTime = 0L

@Composable
private fun TopBar(
    scaffoldState: ScaffoldState,
    currentRoute: String?,
    mainNav: NavHostController,
    vm: TitleViewModel = hiltViewModel(mainNav.getBackStackEntry(Drawer.route))
) {
    val state by vm.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Column {
                Text(text = state.title)
                if (state.subtitle.isNotEmpty()) Text(
                    text = state.subtitle,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    scaffoldState.drawerState.open()
                }
            }) { Icon(Icons.Default.Menu, "") }
        },
        actions = { MAP_SCREENS_ROUTE[currentRoute]?.actions?.invoke(this, mainNav) },
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false, applyTop = false
        )
    )
}

// Боковое меню с выбором пунктов для навигации по приложению
@Composable
private fun DrawerContent(
    viewModel: DrawerViewModel,
    scaffoldState: ScaffoldState,
    currentRoute: String?,
    navControlller: NavHostController
) {
    val menuItems by viewModel.loadMainMenuItems().collectAsState(initial = listOf())
    val coroutineScope = rememberCoroutineScope()

    val insets = LocalWindowInsets.current

    val barsStart = with(LocalDensity.current) { insets.systemBars.left.toDp() }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .padding(start = barsStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(id = R.mipmap.ic_launcher_foreground), "")
            Column {
                Text(
                    text = stringResource(id = R.string.app_name_version, BuildConfig.VERSION_NAME)
                )
                Text(text = stringResource(id = R.string.name))
            }
        }
        // Навигация по пунктам приложения
        LazyColumn(
            contentPadding = PaddingValues(start = barsStart)
        ) {
            itemsIndexed(items = menuItems) { index, item ->
                MAP_SCREENS_TYPE[item.type]?.let { screen ->
                    MainMenuItemRows(index, menuItems.size, item, screen.icon, loadData(item)) {
                        if (currentRoute != screen.route)
                            navControlller.navigate(screen.route)
                        coroutineScope.launch {
                            scaffoldState.drawerState.close()
                        }
                    }
                }
            }
        }
    }
}

// Шаблон пункта меню
@Composable
private fun MainMenuItemRows(
    index: Int,
    max: Int,
    item: MainMenuItem,
    icon: ImageVector,
    add: String,
    action: () -> Unit
) {
    val viewModel: DrawerViewModel = viewModel()
    val editMode by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = action)
    ) {
        Icon(icon, "", modifier = Modifier.padding(16.dp))

        Text(text = item.name, style = MaterialTheme.typography.h6)

        if (!editMode)
            Text(
                text = add,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

        if (editMode) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.swapMenuItems(index, index - 1) },
                    enabled = index > 0
                ) {
                    Icon(Icons.Default.ArrowDropUp, "")
                }

                IconButton(
                    onClick = { viewModel.swapMenuItems(index, index + 1) },
                    enabled = index < max - 1
                ) {
                    Icon(Icons.Default.ArrowDropDown, "")
                }
            }
        }
    }
}

@Composable
private fun loadData(
    item: MainMenuItem,
    viewModel: DrawerViewModel = hiltViewModel()
): String {
    return when (item.type) {
        MainMenuType.Default,
        MainMenuType.Library -> {
            val count by viewModel.loadLibraryCounts().collectAsState(0)
            count.toString()
        }
        MainMenuType.Storage -> {
            val count by viewModel.loadStorageSizes().collectAsState(0.0)
            stringResource(R.string.main_menu_storage_size_mb, formatDouble(count))
        }
        MainMenuType.Category -> {
            val count by viewModel.loadCategoriesCount().collectAsState(0)
            count.toString()
        }
        MainMenuType.Catalogs -> {
            val size by viewModel.loadSiteCatalogSize().collectAsState(initial = 0)
            val volume by viewModel.loadSiteCatalogVolume().collectAsState(initial = 0)
            stringResource(id = R.string.main_menu_item_catalogs, size, volume)
        }
        MainMenuType.Downloader -> {
            val count by viewModel.loadDownloadCount().collectAsState(initial = 0)
            count.toString()
        }
        MainMenuType.Latest -> {
            val count by viewModel.loadLatestCount().collectAsState(initial = 0)
            count.toString()
        }
        MainMenuType.Schedule -> {
            val count by viewModel.loadPlannedCount().collectAsState(initial = 0)
            count.toString()
        }
        MainMenuType.Settings,
        MainMenuType.Statistic -> ""
    }
}

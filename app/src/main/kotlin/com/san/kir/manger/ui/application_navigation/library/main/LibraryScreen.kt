package com.san.kir.manger.ui.application_navigation.library.main

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.core.compose_utils.TopBarScreenContent
import com.san.kir.core.support.MainMenuType
import com.san.kir.core.utils.TestTags
import com.san.kir.core.utils.coroutines.mainLaunch
import com.san.kir.core.utils.formatDouble
import com.san.kir.core.utils.toast
import com.san.kir.data.models.base.MainMenuItem
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import kotlinx.coroutines.launch

private var backPressedTime = 0L

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryScreen(
    navigateToScreen: (MainMenuType) -> Unit,
    navigateToCategories: () -> Unit,
    navigateToCatalogs: () -> Unit,
    navigateToInfo: (String) -> Unit,
    navigateToStorage: (String) -> Unit,
    navigateToStats: (String) -> Unit,
    navigateToChapters: (String) -> Unit,
    navigateToOnline: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val context = LocalContext.current as? ComponentActivity
    val coroutineScope = rememberCoroutineScope()

    TopBarScreenContent(
        title = stringResource(R.string.library_title),
        additionalPadding = 0.dp,
        scaffoldState = scaffoldState,
        drawerContent = { DrawerContent(scaffoldState, navigateToScreen) },
        actions = { LibraryActions(navigateToOnline, viewModel) },
        enableCollapsingBars = false,
    ) {
        LibraryContent(
            navigateToCatalogs = navigateToCatalogs,
            navigateToInfo = navigateToInfo,
            navigateToStorage = navigateToStorage,
            navigateToStats = navigateToStats,
            navigateToChapters = navigateToChapters,
            navigateToCategories = navigateToCategories,
            viewModel = viewModel,
        )
    }

    BackHandler {
        coroutineScope.mainLaunch {
            if (scaffoldState.drawerState.isOpen) {
                scaffoldState.drawerState.close()
            } else {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    context?.finish()
                } else {
                    context?.toast(R.string.first_run_exit_text)
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }
}

// Боковое меню с выбором пунктов для навигации по приложению
@Composable
private fun DrawerContent(
    scaffoldState: ScaffoldState,
    navigateToScreen: (MainMenuType) -> Unit,
    viewModel: DrawerViewModel = hiltViewModel(),
) {
    val menuItems by viewModel.loadMainMenuItems().collectAsState(initial = listOf())
    val coroutineScope = rememberCoroutineScope()

    val insets = LocalWindowInsets.current
    val barsStart = with(LocalDensity.current) { insets.systemBars.left.toDp() }
    val barsBottom = with(LocalDensity.current) { insets.systemBars.bottom.toDp() }

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
            Image(painterResource(R.mipmap.ic_launcher_foreground), "")
            Column {
                Text(
                    text = stringResource(id = R.string.app_name_version, BuildConfig.VERSION_NAME)
                )
                Text(text = stringResource(id = R.string.name))
            }
        }
        // Навигация по пунктам приложения
        LazyColumn(
            contentPadding = PaddingValues(start = barsStart, bottom = barsBottom)
        ) {
            itemsIndexed(items = menuItems) { index, item ->
                MainMenuItemRows(index, menuItems.size, item, loadData(item)) {
                    navigateToScreen(item.type)
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
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
    add: String,
    viewModel: DrawerViewModel = hiltViewModel(),
    action: () -> Unit,
) {
    val editMode by viewModel.editMenu.collectAsState(false)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = action)
            .testTag(TestTags.Drawer.item)
    ) {
        Icon(item.type.icon, "", modifier = Modifier.padding(16.dp))

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
    viewModel: DrawerViewModel = hiltViewModel(),
): String {
    return when (item.type) {
        MainMenuType.Default,
        MainMenuType.Library,
        -> {
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
        MainMenuType.Schedule -> viewModel.plannedCount.collectAsState().value.toString()
        MainMenuType.Accounts,
        MainMenuType.Settings,
        MainMenuType.Statistic,
        -> ""
    }
}

package com.san.kir.manger.ui.drawer

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.ui.drawer.catalogs.CatalogsActions
import com.san.kir.manger.ui.drawer.catalogs.CatalogsScreen
import com.san.kir.manger.ui.drawer.categories.CategoriesActions
import com.san.kir.manger.ui.drawer.categories.CategoriesScreen
import com.san.kir.manger.ui.drawer.library.LibraryActions
import com.san.kir.manger.ui.drawer.library.LibraryScreen
import com.san.kir.manger.ui.drawer.storage.StorageScreen
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.view_models.DrawerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed class DrawerNavScreen(
    val route: String,
    val type: MainMenuType,
    val icon: ImageVector,
    val actions: (@Composable RowScope.(mainNavController: NavHostController) -> Unit) = {},
    val content: @Composable (
        navController: NavHostController,
        mainNavController: NavHostController,
        contentPadding: PaddingValues,
    ) -> Unit
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
val ALL_SCREENS =
    listOf(
        LibraryNavScreen,
        StorageNavScreen,
        CategoriesNavScreen,
        CatalogsNavScreen,
        Downloader,
        Latest,
        Settings,
        Statistic,
        Schedule
    )

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
val MAP_SCREENS_TYPE = ALL_SCREENS.associateBy { it.type }

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
val MAP_SCREENS_ROUTE = ALL_SCREENS.associateBy { it.route }


@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@SuppressLint("RestrictedApi")
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
object LibraryNavScreen : DrawerNavScreen(
    route = "library",
    type = MainMenuType.Library,
    icon = Icons.Default.LocalLibrary,
    content = { nav, mainNav, cp -> LibraryScreen(nav, mainNav, cp) },
    actions = { mainNav -> LibraryActions(mainNav) }
)

@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
object StorageNavScreen : DrawerNavScreen(
    route = "storage",
    type = MainMenuType.Storage,
    icon = Icons.Default.Storage,
    content = { _, mainNav, cp -> StorageScreen(mainNav, cp) }
)

@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
object CategoriesNavScreen : DrawerNavScreen(
    route = "categories",
    type = MainMenuType.Category,
    icon = Icons.Default.Category,
    content = { _, mainNav, cp -> CategoriesScreen(mainNav, cp) },
    actions = { mainNav -> CategoriesActions(mainNav) }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object CatalogsNavScreen : DrawerNavScreen(
    route = "catalogs",
    type = MainMenuType.Catalogs,
    icon = Icons.Default.FormatListBulleted,
    content = { _, mainNav, cp -> CatalogsScreen(mainNav, cp) },
    actions = { CatalogsActions(it) }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Downloader : DrawerNavScreen(
    route = "downloader",
    type = MainMenuType.Downloader,
    icon = Icons.Default.GetApp,
    content = { _, _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Latest : DrawerNavScreen(
    route = "latest",
    type = MainMenuType.Latest,
    icon = Icons.Default.History,
    content = { _, _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Settings : DrawerNavScreen(
    route = "settings",
    type = MainMenuType.Settings,
    icon = Icons.Default.Settings,
    content = { _, _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Statistic : DrawerNavScreen(
    route = "statistic",
    type = MainMenuType.Statistic,
    icon = Icons.Default.Note,
    content = { _, _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Schedule : DrawerNavScreen(
    route = "schedule",
    type = MainMenuType.Schedule,
    icon = Icons.Default.Schedule,
    content = { _, _, _ -> }
)

@Composable
fun loadData(
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

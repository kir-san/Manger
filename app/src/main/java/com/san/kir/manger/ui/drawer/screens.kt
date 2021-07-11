package com.san.kir.manger.ui.drawer

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.ui.MainViewModel
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

sealed class AppScreen(
    val route: String,
    val type: MainMenuType,
    val icon: ImageVector,
    val actions: (@Composable RowScope.(mainNavController: NavHostController) -> Unit) = {},
    val content: @Composable (
        navController: NavHostController,
        mainNavController: NavHostController
    ) -> Unit
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
val ALL_SCREENS =
    listOf(
        Library,
        Storage,
        Categories,
        Catalogs,
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
object Library : AppScreen(
    route = "library",
    type = MainMenuType.Library,
    icon = Icons.Default.LocalLibrary,
    content = { nav, mainNav -> LibraryScreen(nav, mainNav) },
    actions = { mainNav -> LibraryActions(mainNav) }
)

@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
object Storage : AppScreen(
    route = "storage",
    type = MainMenuType.Storage,
    icon = Icons.Default.Storage,
    content = { _, mainNav -> StorageScreen(mainNav) }
)

@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
object Categories : AppScreen(
    route = "categorie",
    type = MainMenuType.Category,
    icon = Icons.Default.Category,
    content = { _, mainNav -> CategoriesScreen(mainNav) },
    actions = { mainNav -> CategoriesActions(mainNav) }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Catalogs : AppScreen(
    route = "catalogs",
    type = MainMenuType.Catalogs,
    icon = Icons.Default.FormatListBulleted,
    content = { _, mainNav -> CatalogsScreen(mainNav) },
    actions = { CatalogsActions(it) }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Downloader : AppScreen(
    route = "downloader",
    type = MainMenuType.Downloader,
    icon = Icons.Default.GetApp,
    content = { _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Latest : AppScreen(
    route = "latest",
    type = MainMenuType.Latest,
    icon = Icons.Default.History,
    content = {  _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Settings : AppScreen(
    route = "settings",
    type = MainMenuType.Settings,
    icon = Icons.Default.Settings,
    content = { _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Statistic : AppScreen(
    route = "statistic",
    type = MainMenuType.Statistic,
    icon = Icons.Default.Note,
    content = { _, _ -> }
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
object Schedule : AppScreen(
    route = "schedule",
    type = MainMenuType.Schedule,
    icon = Icons.Default.Schedule,
    content = {  _, _ -> }
)

@Composable
fun loadData(item: MainMenuItem,
             viewModel: DrawerViewModel = hiltViewModel()): String {
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

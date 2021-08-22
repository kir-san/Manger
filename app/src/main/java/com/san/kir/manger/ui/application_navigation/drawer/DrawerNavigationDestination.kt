package com.san.kir.manger.ui.application_navigation.drawer

import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.createGraph
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.drawer.catalogs.CatalogsActions
import com.san.kir.manger.ui.application_navigation.drawer.catalogs.CatalogsScreen
import com.san.kir.manger.ui.application_navigation.drawer.categories.CategoriesActions
import com.san.kir.manger.ui.application_navigation.drawer.categories.CategoriesScreen
import com.san.kir.manger.ui.application_navigation.drawer.library.LibraryActions
import com.san.kir.manger.ui.application_navigation.drawer.library.LibraryScreen
import com.san.kir.manger.ui.application_navigation.drawer.storage.StorageScreen
import com.san.kir.manger.utils.enums.MainMenuType

val MAP_SCREENS_TYPE = DrawerNavigationDestination.values().associateBy { it.type }

val MAP_SCREENS_ROUTE = DrawerNavigationDestination.values().associateBy { it.route }


enum class DrawerNavigationDestination(
    val route: String,
    val type: MainMenuType,
    val icon: ImageVector,
    val actions: (@Composable RowScope.(mainNavController: NavHostController) -> Unit) = {},
    val content: @Composable (
        navController: NavHostController,
        mainNavController: NavHostController,
        contentPadding: PaddingValues,
    ) -> Unit
) {
    Library(
        route = "library",
        type = MainMenuType.Library,
        icon = Icons.Default.LocalLibrary,
        content = { nav, mainNav, cp -> LibraryScreen(nav, mainNav, cp) },
        actions = { mainNav -> LibraryActions(mainNav) }
    ),

    Storage(
        route = "storage",
        type = MainMenuType.Storage,
        icon = Icons.Default.Storage,
        content = { _, mainNav, cp -> StorageScreen(mainNav, cp) }
    ),

    Categories(
        route = "categories",
        type = MainMenuType.Category,
        icon = Icons.Default.Category,
        content = { _, mainNav, cp -> CategoriesScreen(mainNav, cp) },
        actions = { mainNav -> CategoriesActions(mainNav) }
    ),

    Catalogs(
        route = "catalogs",
        type = MainMenuType.Catalogs,
        icon = Icons.Default.FormatListBulleted,
        content = { _, mainNav, cp -> CatalogsScreen(mainNav, cp) },
        actions = { CatalogsActions(it) }
    ),

    Downloader(
        route = "downloader",
        type = MainMenuType.Downloader,
        icon = Icons.Default.GetApp,
        content = { _, _, _ -> }
    ),

    Latest(
        route = "latest",
        type = MainMenuType.Latest,
        icon = Icons.Default.History,
        content = { _, _, _ -> }
    ),

    Settings(
        route = "settings",
        type = MainMenuType.Settings,
        icon = Icons.Default.Settings,
        content = { _, _, _ -> }
    ),

    Statistic(
        route = "statistic",
        type = MainMenuType.Statistic,
        icon = Icons.Default.Note,
        content = { _, _, _ -> }
    ),

    Schedule(
        route = "schedule",
        type = MainMenuType.Schedule,
        icon = Icons.Default.Schedule,
        content = { _, _, _ -> }
    ),
}

@OptIn(ExperimentalAnimationApi::class)
fun NavHostController.drawerGraph(mainNav: NavHostController, contentPadding: PaddingValues) =
    createGraph(DrawerNavigationDestination.Library.route, null) {
        DrawerNavigationDestination.values().forEach { screen ->
            composable(
                route = screen.route,
                content = { screen.content(this@drawerGraph, mainNav, contentPadding) })
        }
    }



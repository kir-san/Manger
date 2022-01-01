package com.san.kir.manger.ui.application_navigation

import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.composable
import com.san.kir.core.support.MainMenuType
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.application_navigation.catalog.catalogsNavGraph
import com.san.kir.manger.ui.application_navigation.categories.CategoriesNavTarget
import com.san.kir.manger.ui.application_navigation.categories.categoriesNavGraph
import com.san.kir.manger.ui.application_navigation.download.DownloadScreen
import com.san.kir.ui.latest.LatestScreen
import com.san.kir.manger.ui.application_navigation.library.LibraryNavTarget
import com.san.kir.manger.ui.application_navigation.library.libraryNavGraph
import com.san.kir.manger.ui.application_navigation.schedule.ScheduleNavTarget
import com.san.kir.manger.ui.application_navigation.schedule.scheduleNavGraph
import com.san.kir.manger.ui.application_navigation.settings.SettingsScreen
import com.san.kir.manger.ui.application_navigation.startapp.StartAppScreen
import com.san.kir.manger.ui.application_navigation.statistic.StatisticNavTarget
import com.san.kir.manger.ui.application_navigation.statistic.statisticNavGraph
import com.san.kir.manger.ui.application_navigation.storage.StorageNavTarget
import com.san.kir.manger.ui.application_navigation.storage.storageNavGraph
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.ui.viewer.MangaViewer

private val values = listOf(
    MainNavTarget.Library,
    MainNavTarget.Storage,
    MainNavTarget.Categories,
    MainNavTarget.Catalogs,
    MainNavTarget.Downloader,
    MainNavTarget.Latest,
    MainNavTarget.Settings,
    MainNavTarget.Statistic,
    MainNavTarget.Schedule,
)

sealed class MainNavTarget(
    val type: MainMenuType,
    val icon: ImageVector,
) : NavTarget {
    object StartApp : MainNavTarget(
        type = MainMenuType.Library,
        icon = Icons.Default.LocalLibrary
    ) {
        override val route: String = "start"
    }

    object Library : MainNavTarget(
        type = MainMenuType.Library,
        icon = Icons.Default.LocalLibrary
    ) {
        override val route: String = "library"
    }

    object Storage : MainNavTarget(
        type = MainMenuType.Storage,
        icon = Icons.Default.Storage
    ) {
        override val route = "storage"
    }

    object Categories : MainNavTarget(
        type = MainMenuType.Category,
        icon = Icons.Default.Category,
    ) {
        override val route = "categories"
    }

    object Catalogs : MainNavTarget(
        type = MainMenuType.Catalogs,
        icon = Icons.Default.FormatListBulleted
    ) {
        override val route: String = "catalogs"
    }

    object Downloader : MainNavTarget(
        type = MainMenuType.Downloader,
        icon = Icons.Default.GetApp
    ) {
        override val route = "downloader"
    }

    object Latest : MainNavTarget(
        type = MainMenuType.Latest,
        icon = Icons.Default.History
    ) {
        override val route = "latest"
    }

    object Settings : MainNavTarget(
        type = MainMenuType.Settings,
        icon = Icons.Default.Settings
    ) {
        override val route = "settings"
    }

    object Statistic : MainNavTarget(
        type = MainMenuType.Statistic,
        icon = Icons.Default.Note
    ) {
        override val route = "statistic"
    }

    object Schedule : MainNavTarget(
        type = MainMenuType.Schedule,
        icon = Icons.Default.Schedule
    ) {
        override val route = "schedule"
    }
}

val MAP_SCREENS_TYPE = values.associateBy { it.type }

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.mainNavGraph(nav: NavHostController) {
    composable(route = MainNavTarget.StartApp.route) {
        StartAppScreen(nav)
    }

    navigation(
        startDestination = LibraryNavTarget.Main.route,
        route = MainNavTarget.Library.route
    ) {
        libraryNavGraph(nav)
    }

    navigation(
        startDestination = StorageNavTarget.Main.route,
        route = MainNavTarget.Storage.route
    ) {
        storageNavGraph(nav)
    }

    navigation(
        startDestination = CategoriesNavTarget.Main.route,
        route = MainNavTarget.Categories.route
    ) {
        categoriesNavGraph(nav)
    }

    navigation(
        startDestination = CatalogsNavTarget.Main.route,
        route = MainNavTarget.Catalogs.route,
    ) {
        catalogsNavGraph(nav)
    }

    composable(
        route = MainNavTarget.Downloader.route,
        deepLinks = listOf(navDeepLink { uriPattern = MainNavTarget.Downloader.deepLink })
    ) {
        DownloadScreen(nav)
    }

    composable(
        route = MainNavTarget.Latest.route,
        deepLinks = listOf(navDeepLink { uriPattern = MainNavTarget.Latest.deepLink })
    ) {
        val context = LocalContext.current
        LatestScreen(
            navigateUp = nav::navigateUp,
            navigateToViewer = { MangaViewer.start(context, it.id) },
            viewModel = hiltViewModel()
        )
    }

    composable(route = MainNavTarget.Settings.route) {
        SettingsScreen(nav)
    }

    navigation(
        startDestination = StatisticNavTarget.Main.route,
        route = MainNavTarget.Statistic.route
    ) {
        statisticNavGraph(nav)
    }

    navigation(
        startDestination = ScheduleNavTarget.Main.route,
        route = MainNavTarget.Schedule.route
    ) {
        scheduleNavGraph(nav)
    }
}



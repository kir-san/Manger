package com.san.kir.manger.ui.application_navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.chapters.ui.download.DownloadsScreen
import com.san.kir.chapters.ui.latest.LatestScreen
import com.san.kir.core.support.MainMenuType
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.ui.application_navigation.accounts.accountsNavGraph
import com.san.kir.manger.ui.application_navigation.catalog.catalogsNavGraph
import com.san.kir.manger.ui.application_navigation.schedule.scheduleNavGraph
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.composable
import com.san.kir.manger.utils.compose.deepLinkIntent
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.settings.ui.settings.SettingsScreen

enum class MainNavTarget(
    val type: MainMenuType,
) : NavTarget {
    Library(MainMenuType.Library) {
        override val content = navTarget(route = "library")
    },

    Storage(MainMenuType.Storage) {
        override val content = navTarget(route = "storage")
    },

    Categories(MainMenuType.Category) {
        override val content = navTarget(route = "categories")
    },

    Catalogs(MainMenuType.Catalogs) {
        override val content = navTarget(route = "catalogs")
    },

    Downloader(MainMenuType.Downloader) {
        override val content = navTarget(route = "downloader", hasDeepLink = true) {
            DownloadsScreen(::navigateUp)
        }
    },

    Latest(MainMenuType.Latest) {
        override val content = navTarget(route = "latest", hasDeepLink = true) {
            val context = LocalContext.current

            MangaUpdaterService.setLatestDeepLink(
                context,
                context.deepLinkIntent<MainActivity>(Latest),
            )

            LatestScreen(
                navigateUp = ::navigateUp,
                navigateToViewer = { MangaViewer.start(context, it) },
            )
        }
    },

    Settings(MainMenuType.Settings) {
        override val content = navTarget(route = "settings") { SettingsScreen(::navigateUp) }
    },

    Statistic(MainMenuType.Statistic) {
        override val content = navTarget(route = "statistic")
    },

    Schedule(MainMenuType.Schedule) {
        override val content = navTarget(route = "schedule")
    },

    Accounts(MainMenuType.Accounts) {
        override val content = navTarget(route = "accounts")
    },
}

private val targets = MainNavTarget.values()
val mainMenuItems = targets.associateBy { it.type }

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun MainNavGraph(nav: NavHostController) {
    AnimatedNavHost(
        navController = nav,
        startDestination = MainNavTarget.Library.content.route(),
    ) {
        composable(nav = nav, target = MainNavTarget.Downloader)
        composable(nav = nav, target = MainNavTarget.Latest)
        composable(nav = nav, target = MainNavTarget.Settings)

        libraryNavGraph(nav)
        storageNavGraph(nav)
        categoriesNavGraph(nav)
        statisticNavGraph(nav)
        scheduleNavGraph(nav)
        catalogsNavGraph(nav)
        accountsNavGraph(nav)
    }
}



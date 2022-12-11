package com.san.kir.manger.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.san.kir.chapters.ui.download.DownloadsScreen
import com.san.kir.chapters.ui.latest.LatestScreen
import com.san.kir.core.support.MainMenuType
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.composable
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.settings.ui.settings.SettingsScreen

enum class MainNavTarget(
    val type: MainMenuType,
) : NavTarget {
    Library(MainMenuType.Library) {
        override val content = navTarget(route = GraphTree.Library())
    },

    Storage(MainMenuType.Storage) {
        override val content = navTarget(route = GraphTree.Storage())
    },

    Categories(MainMenuType.Category) {
        override val content = navTarget(route = GraphTree.Categories())
    },

    Catalogs(MainMenuType.Catalogs) {
        override val content = navTarget(route = GraphTree.Catalogs())
    },

    Downloader(MainMenuType.Downloader) {
        override val content = navTarget(
            route = GraphTree.downloader,
            hasDeepLink = true
        ) {
            DownloadsScreen(navigateUp())
        }
    },

    Latest(MainMenuType.Latest) {
        override val content = navTarget(
            route = GraphTree.latest,
            hasDeepLink = true
        ) {
            val context = LocalContext.current

            val navigateTo: (Long) -> Unit = remember { { MangaViewer.start(context, it) } }

            LatestScreen(
                navigateUp = navigateUp(),
                navigateToViewer = navigateTo,
            )
        }
    },

    Settings(MainMenuType.Settings) {
        override val content = navTarget(route = GraphTree.settings) {
            SettingsScreen(navigateUp())
        }
    },

    Statistic(MainMenuType.Statistic) {
        override val content = navTarget(route = GraphTree.Statistic())
    },

    Schedule(MainMenuType.Schedule) {
        override val content = navTarget(route = GraphTree.Schedule())
    },

    Accounts(MainMenuType.Accounts) {
        override val content = navTarget(route = GraphTree.Accounts())
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



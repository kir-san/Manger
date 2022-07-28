package com.san.kir.manger.ui.application_navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.support.MainMenuType
import com.san.kir.features.latest.LatestScreen
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.ui.application_navigation.accounts.accountsNavGraph
import com.san.kir.manger.ui.application_navigation.catalog.catalogsNavGraph
import com.san.kir.manger.ui.application_navigation.categories.categoriesNavGraph
import com.san.kir.manger.ui.application_navigation.download.DownloadScreen
import com.san.kir.manger.ui.application_navigation.library.libraryNavGraph
import com.san.kir.manger.ui.application_navigation.schedule.scheduleNavGraph
import com.san.kir.manger.ui.application_navigation.settings.SettingsScreen
import com.san.kir.manger.ui.application_navigation.startapp.StartAppScreen
import com.san.kir.manger.ui.application_navigation.statistic.statisticNavGraph
import com.san.kir.manger.ui.application_navigation.storage.storageNavGraph
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.composable
import com.san.kir.manger.utils.compose.deepLinkIntent
import com.san.kir.manger.utils.compose.navTarget
import timber.log.Timber

enum class MainNavTarget(
    val type: MainMenuType,
) : NavTarget {
    Splash(MainMenuType.Library) {
        override val content = navTarget(route = "start") {
            StartAppScreen {
                Timber.w("Go to library")
                navigate(Library)
            }
        }
    },

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
            DownloadScreen(::navigateUp)
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
                navigateToViewer = { MangaViewer.start(context, it.id) },
                viewModel = hiltViewModel()
            )
        }
    },

    Settings(MainMenuType.Settings) {
        override val content = navTarget(route = "settings") {
            SettingsScreen(::navigateUp)
        }
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
        startDestination = MainNavTarget.Splash.content.route(),
    ) {
        composable(nav = nav, target = MainNavTarget.Splash)
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



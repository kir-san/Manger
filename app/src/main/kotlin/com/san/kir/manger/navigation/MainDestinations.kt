package com.san.kir.manger.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.san.kir.chapters.ui.download.DownloadsScreen
import com.san.kir.chapters.ui.latest.LatestScreen
import com.san.kir.core.support.MainMenuType
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.manger.navigation.utils.Constants
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
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
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

@OptIn(ExperimentalAnimationApi::class)
private val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition = {
//    Timber.tag("animation").d("main enter init   -> ${initialState.destination.route}")

    val target = targetState.destination.route
    val initial = initialState.destination.route
    if (target != null && initial != null && GraphTree.Library.main in initial)
        when {
            GraphTree.Statistic.item in target ||
                    GraphTree.Storage.item in target ->
                slideInVertically(
                    animationSpec = tween(Constants.duration),
                    initialOffsetY = { it }
                )

            else                                     ->
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.End,
                    animationSpec = tween(Constants.duration)
                )
        }
    else
        slideIntoContainer(
            AnimatedContentScope.SlideDirection.End,
            animationSpec = tween(Constants.duration)
        )
}

@OptIn(ExperimentalAnimationApi::class)
private val exitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition = {
//    Timber.tag("animation").d("main exit target  -> ${targetState.destination.route}")
//    Timber.tag("animation").d("main exit initial -> ${initialState.destination.route}")

    val target = targetState.destination.route
    val initial = initialState.destination.route

    if (initial != null
        && GraphTree.Library.main in initial
        && target != null
        && (GraphTree.Library.addOnline in target
                || GraphTree.Library.item in target
                || GraphTree.Library.about in target
                || GraphTree.Statistic.item in target
                || GraphTree.Storage.item in target))
        fadeOut(animationSpec = tween(Constants.duration))
    else
        slideOutOfContainer(
            AnimatedContentScope.SlideDirection.End, animationSpec = tween(Constants.duration)
        )
}

@OptIn(ExperimentalAnimationApi::class)
private val popEnterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition = {
//    Timber.tag("animation").d("main popEnter init   -> ${initialState.destination.route}")
//    Timber.tag("animation").d("main popEnter target -> ${targetState.destination.route}")

    val target = targetState.destination.route
    val initial = initialState.destination.route

    if (target != null
        && GraphTree.Library.main in target
        && initial != null
        && (GraphTree.Library.addOnline in initial
                || GraphTree.Library.item in initial
                || GraphTree.Library.about in initial
                || GraphTree.Statistic.item in initial
                || GraphTree.Storage.item in initial))
        fadeIn(animationSpec = tween(Constants.duration))
    else
        slideIntoContainer(
            AnimatedContentScope.SlideDirection.Start, animationSpec = tween(Constants.duration)
        )
}

@OptIn(ExperimentalAnimationApi::class)
private val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition = {
//    Timber.tag("animation").d("main popExit target -> ${targetState.destination.route}")

    val initial = initialState.destination.route
    val target = targetState.destination.route

    if (initial != null && target != null && GraphTree.Library.main in target)
        when {
            GraphTree.Statistic.item in initial ||
                    GraphTree.Storage.item in initial ->
                slideOutVertically(
                    animationSpec = tween(Constants.duration),
                    targetOffsetY = { it }
                )

            else                                      ->
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Start,
                    animationSpec = tween(Constants.duration)
                )
        }
    else
        slideOutOfContainer(
            AnimatedContentScope.SlideDirection.Start,
            animationSpec = tween(Constants.duration)
        )
}

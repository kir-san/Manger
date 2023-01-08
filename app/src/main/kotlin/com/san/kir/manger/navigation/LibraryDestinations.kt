package com.san.kir.manger.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.catalog.ui.addOnline.AddOnlineScreen
import com.san.kir.chapters.ui.chapters.ChaptersScreen
import com.san.kir.core.support.MainMenuType
import com.san.kir.features.viewer.MangaViewer
import com.san.kir.library.ui.library.LibraryNavigation
import com.san.kir.library.ui.library.LibraryScreen
import com.san.kir.library.ui.mangaAbout.MangaAboutScreen
import com.san.kir.manger.navigation.utils.Constants
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation

enum class LibraryNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Library.main) {
            val navigation = remember {
                LibraryNavigation(
                    navigateToScreen = { type ->
                        if (MainMenuType.Library != type)
                            mainMenuItems[type]?.let { navigate(it) }
                    },
                    navigateToCategories = { navigate(MainNavTarget.Categories) },
                    navigateToCatalogs = { navigate(MainNavTarget.Catalogs) },
                    navigateToInfo = { navigate(About, it) },
                    navigateToStorage = { navigate(StorageNavTarget.Storage, it, true) },
                    navigateToStats = { navigate(StatisticNavTarget.Statistic, it) },
                    navigateToChapters = { navigate(Chapters, it) },
                    navigateToOnline = { navigate(AddOnline) },
                )
            }

            LibraryScreen(navigation)
        }
    },

    Chapters {
        override val content = navTarget(
            route = GraphTree.Library.item,
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            val context = LocalContext.current
            val navigate: (Long) -> Unit = remember { { MangaViewer.start(context, it) } }

            ChaptersScreen(
                navigateUp = navigateUp(),
                navigateToViewer = navigate,
                mangaId = longElement() ?: -1L
            )
        }
    },

    AddOnline {
        override val content = navTarget(route = GraphTree.Library.addOnline) {
            AddOnlineScreen(
                navigateUp = navigateUp(),
                navigateToNext = rememberNavigateString(CatalogsNavTarget.AddLocal)
            )
        }
    },

    About {
        override val content = navTarget(
            route = GraphTree.Library.about,
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            MangaAboutScreen(navigateUp(), longElement() ?: -1)
        }
    };
}

private val targets = LibraryNavTarget.values().toList()

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.libraryNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = LibraryNavTarget.Main,
        route = MainNavTarget.Library,
        targets = targets,
        enterTransition = enterTransition,
        popExitTransition = popExitTransition
    )
}

@OptIn(ExperimentalAnimationApi::class)
private val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = targetState.destination.route
    if (target == null) null
    else
        when {
            GraphTree.Library.addOnline in target ->
                scaleIn(
                    animationSpec = tween(Constants.duration),
                    initialScale = 0.08f,
                    transformOrigin = TransformOrigin(0.9f, 0.05f)
                )

            GraphTree.Library.item in target      ->
                expandIn(
                    animationSpec = tween(Constants.duration),
                    expandFrom = Alignment.Center
                )

            GraphTree.Library.about in target     ->
                slideInVertically(
                    animationSpec = tween(Constants.duration),
                    initialOffsetY = { it }
                )

            else                                  -> null
        }
}

@OptIn(ExperimentalAnimationApi::class)
private val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val initial = initialState.destination.route

    if (initial == null) null
    else
        when {
            GraphTree.Library.addOnline in initial ->
                scaleOut(
                    animationSpec = tween(Constants.duration),
                    transformOrigin = TransformOrigin(0.9f, 0.05f)
                )

            GraphTree.Library.item in initial      ->
                shrinkOut(
                    animationSpec = tween(Constants.duration),
                    shrinkTowards = Alignment.Center
                )

            GraphTree.Library.about in initial     ->
                slideOutVertically(
                    animationSpec = tween(Constants.duration),
                    targetOffsetY = { it }
                )

            else                                   -> null
        }
}

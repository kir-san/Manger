package com.san.kir.manger.ui.application_navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.chapters.ChaptersScreen
import com.san.kir.core.support.MainMenuType
import com.san.kir.library.ui.addOnline.AddOnlineScreen
import com.san.kir.library.ui.library.LibraryNavigation
import com.san.kir.library.ui.library.LibraryScreen
import com.san.kir.library.ui.mangaAbout.MangaAboutScreen
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class LibraryNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            LibraryScreen(
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
            )
        }
    },

    Chapters {
        override val content = navTarget(route = "chapters", hasItems = true) {
            ChaptersScreen(hiltViewModel(), stringElement ?: "", ::navigateUp)
        }
    },

    AddOnline {
        override val content = navTarget(route = "add_online") {
            AddOnlineScreen(
                navigateUp = ::navigateUp,
                navigateToNext = { arg -> navigate(CatalogsNavTarget.AddLocal, arg) }
            )
        }
    },

    About {
        override val content = navTarget(
            route = "about",
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            MangaAboutScreen(::navigateUp, longElement ?: -1)
        }
    };
}

private val targets = LibraryNavTarget.values().toList()

fun NavGraphBuilder.libraryNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = LibraryNavTarget.Main,
        route = MainNavTarget.Library,
        targets = targets
    )
}

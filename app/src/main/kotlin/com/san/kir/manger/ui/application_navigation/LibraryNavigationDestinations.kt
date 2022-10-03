package com.san.kir.manger.ui.application_navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.chapters.ChaptersScreen
import com.san.kir.core.support.MainMenuType
import com.san.kir.library.ui.library.LibraryNavigation
import com.san.kir.library.ui.library.LibraryScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAboutScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddOnlineScreen
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.application_navigation.storage.StorageNavTarget
import com.san.kir.manger.ui.onlyMangaViewModel
import com.san.kir.manger.utils.compose.NavTarget
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
                    navigateToStorage = { navigate(StorageNavTarget.Storage, it) },
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
            MangaAddOnlineScreen(
                navigateUp = ::navigateUp,
                navigateToNext = { arg ->
                    navigate(CatalogsNavTarget.AddLocal, arg)
                }
            )
        }
    },

    About {
        override val content = navTarget(
            route = "about",
            hasItems = true,
        ) {
            val viewModel = onlyMangaViewModel(mangaUnic = stringElement ?: "")

            val manga by viewModel.manga.collectAsState()
            val categoryName by viewModel.categoryName.collectAsState()

            MangaAboutScreen(::navigateUp, manga, categoryName)
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

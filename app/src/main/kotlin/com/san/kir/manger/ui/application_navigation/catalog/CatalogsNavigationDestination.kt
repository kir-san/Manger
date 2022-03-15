package com.san.kir.manger.ui.application_navigation.catalog

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaInfoScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.siteCatalogItemViewModel
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogScreen
import com.san.kir.manger.ui.application_navigation.catalog.catalog.catalogViewModel
import com.san.kir.manger.ui.application_navigation.catalog.global_search.GlobalSearchScreen
import com.san.kir.manger.ui.application_navigation.catalog.global_search.GlobalSearchViewModel
import com.san.kir.manger.ui.application_navigation.catalog.main.CatalogsScreen
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class CatalogsNavTarget : NavTarget {

    Main {
        override val content = navTarget(
            route = "main", hasDeepLink = true,
        ) {
            CatalogsScreen(
                navigateUp = ::navigateUp,
                navigateToSearch = { navigate(GlobalSearch) },
                navigateToItem = { navigate(Catalog, it) }
            )
        }
    },

    Catalog {
        override val content = navTarget(route = "catalog", hasItem = true) {
            val item = stringElement ?: ""
            val viewModel = catalogViewModel(item)

            CatalogScreen(
                navigateUp = ::navigateUp,
                navigateToInfo = { navigate(Info, it) },
                navigateToAdd = { navigate(AddLocal, it) },
                viewModel
            )
        }
    },

    GlobalSearch {
        override val content = navTarget(route = "global_search", hasItem = true) {
            GlobalSearchScreen(
                navigateUp = ::navigateUp,
                navigateToInfo = { navigate(Info, it) },
                navigateToAdd = { navigate(AddLocal, it) },
                searchText = stringElement ?: "",
                viewModel = hiltViewModel())
        }
    },

    Info {
        override val content = navTarget(route = "info", hasItem = true) {
            MangaInfoScreen(
                navigateUp = ::navigateUp,
                navigateToAdd = { navigate(AddLocal, it) },
                siteCatalogItemViewModel(stringElement ?: "")
            )
        }
    },

    AddLocal {
        override val content = navTarget(route = "add", hasItem = true) {
            MangaAddScreen(stringElement ?: "", ::navigateUp)
        }
    };
}

private val targets = CatalogsNavTarget.values().toList()

fun NavGraphBuilder.catalogsNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = CatalogsNavTarget.Main,
        route = MainNavTarget.Catalogs,
        targets = targets,
    )
}


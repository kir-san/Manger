package com.san.kir.manger.ui.application_navigation.catalog

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaInfoScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.siteCatalogItemViewModel
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogScreen
import com.san.kir.manger.ui.application_navigation.catalog.catalog.catalogViewModel
import com.san.kir.manger.ui.application_navigation.catalog.global_search.GlobalSearchScreen
import com.san.kir.manger.ui.application_navigation.catalog.main.CatalogsScreen
import com.san.kir.manger.utils.compose.NavItem
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.SiteCatalogItem
import com.san.kir.manger.utils.compose.SiteItem
import com.san.kir.manger.utils.compose.getStringElement

sealed class CatalogsNavTarget : NavTarget {

    object Main : CatalogsNavTarget() {
        override val route: String = "main"
    }

    object Catalog : CatalogsNavTarget() {
        override val base: String = "catalog"
        override val item: NavItem = SiteItem
        override val isOptional: Boolean = true
    }

    object GlobalSearch : CatalogsNavTarget() {
        override val route: String = "global_search"
        override val isOptional: Boolean = true
    }

    object Info : CatalogsNavTarget() {
        override val base: String = "info"
        override val item: NavItem = SiteCatalogItem
        override val isOptional: Boolean = true
    }

    object AddLocal : CatalogsNavTarget() {
        override val base: String = "add_local"
        override val item: NavItem = SiteCatalogItem
        override val isOptional: Boolean = true
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.catalogsNavGraph(nav: NavHostController) {
    composable(
        route = CatalogsNavTarget.Main.route,
        deepLinks = listOf(navDeepLink { uriPattern = CatalogsNavTarget.Main.deepLink }),
        content = {
            CatalogsScreen(nav)
        }
    )

    composable(
        route = CatalogsNavTarget.Catalog.route,
        content = {
            val item = nav.getStringElement(SiteItem) ?: ""
            val viewModel = catalogViewModel(item)

            CatalogScreen(nav, viewModel)
        }
    )

    composable(
        route = CatalogsNavTarget.GlobalSearch.route,
        content = { back ->
            val initSearchText = back.getStringElement(CatalogsNavTarget.GlobalSearch) ?: ""

            GlobalSearchScreen(nav, initSearchText = initSearchText)
        }
    )

    composable(
        route = CatalogsNavTarget.Info.route,
        content = {
            val item = nav.getStringElement(SiteCatalogItem) ?: ""

            MangaInfoScreen(nav, siteCatalogItemViewModel(item))
        }
    )

    composable(
        route = CatalogsNavTarget.AddLocal.route,
        content = {
            val item = nav.getStringElement(SiteCatalogItem) ?: ""

            MangaAddScreen(item, nav::navigateUp)
        }
    )
}


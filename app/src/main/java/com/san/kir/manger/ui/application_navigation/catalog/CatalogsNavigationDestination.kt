package com.san.kir.manger.ui.application_navigation.catalog

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaInfoScreen
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogScreen
import com.san.kir.manger.ui.application_navigation.catalog.catalog.catalogViewModel
import com.san.kir.manger.ui.application_navigation.catalog.global_search.GlobalSearchScreen
import com.san.kir.manger.ui.application_navigation.catalog.main.CatalogsScreen
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.SiteItem
import com.san.kir.manger.ui.utils.getElement

sealed class CatalogsNavTarget : NavTarget {

    object Main : CatalogsNavTarget() {
        override val route: String = "main"
        override val savedItem: String = "catalog_item"
    }

    object Catalog : CatalogsNavTarget() {
        override val base: String = "catalog/"
        override val route: String = "$base{${SiteItem.value}}"
    }

    object GlobalSearch : CatalogsNavTarget() {
        override val route: String = "global_search"
    }

    object Info : CatalogsNavTarget() {
        override val route: String = "info"
        override val savedItem: String = route + "_item"
    }

    object AddLocal : CatalogsNavTarget() {
        override val route: String = "add_local"
        override val savedItem: String = route + "_item"
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.catalogsNavGraph(nav: NavHostController) {
    composable(
        route = CatalogsNavTarget.Main.route,
        content = {
            CatalogsScreen(nav)
        }
    )

    composable(
        route = CatalogsNavTarget.Catalog.route,
        content = {
            val item = nav.getElement(SiteItem) ?: ""
            val viewModel = catalogViewModel(item)

            CatalogScreen(nav, viewModel)
        }
    )

    composable(
        route = CatalogsNavTarget.GlobalSearch.route,
        content = {
            GlobalSearchScreen(nav)
        }
    )

    composable(
        route = CatalogsNavTarget.Info.route,
        content = {
            val item = nav.getElement(CatalogsNavTarget.Info) ?: SiteCatalogElement()

            MangaInfoScreen(nav, item)
        }
    )

    composable(
        route = CatalogsNavTarget.AddLocal.route,
        content = {
            val item = nav.getElement(CatalogsNavTarget.AddLocal) ?: SiteCatalogElement()

            MangaAddScreen(nav, item)
        }
    )
}


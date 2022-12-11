package com.san.kir.manger.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.catalog.ui.addStandart.AddStandartScreen
import com.san.kir.catalog.ui.catalog.CatalogScreen
import com.san.kir.catalog.ui.catalogItem.CatalogItemScreen
import com.san.kir.catalog.ui.catalogs.CatalogsScreen
import com.san.kir.catalog.ui.search.SearchScreen
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation

enum class CatalogsNavTarget : NavTarget {

    Main {
        override val content = navTarget(
            route = GraphTree.Catalogs.main,
            hasDeepLink = true
        ) {
            CatalogsScreen(
                navigateUp = navigateUp(),
                navigateToSearch = rememberNavigate(GlobalSearch),
                navigateToItem = rememberNavigateString(Catalog)
            )
        }
    },

    Catalog {
        override val content = navTarget(
            route = GraphTree.Catalogs.item,
            hasItems = true
        ) {
            CatalogScreen(
                navigateUp = navigateUp(),
                navigateToInfo = rememberNavigateString(Info),
                navigateToAdd = rememberNavigateString(AddLocal),
                catalogName = stringElement() ?: ""
            )
        }
    },

    GlobalSearch {
        override val content = navTarget(
            route = GraphTree.Catalogs.search,
            hasItems = true
        ) {
            SearchScreen(
                navigateUp = navigateUp(),
                navigateToInfo = rememberNavigateString(Info),
                navigateToAdd = rememberNavigateString(AddLocal),
                searchText = stringElement() ?: "",
            )
        }
    },

    Info {
        override val content = navTarget(
            route = GraphTree.Catalogs.itemInfo,
            hasItems = true
        ) {
            CatalogItemScreen(
                navigateUp = navigateUp(),
                navigateToAdd = rememberNavigateString(AddLocal),
                url = stringElement() ?: ""
            )
        }
    },

    AddLocal {
        override val content = navTarget(
            route = GraphTree.Catalogs.itemAdd,
            hasItems = true
        ) {
            AddStandartScreen(
                navigateUp = navigateUp(),
                url = stringElement() ?: ""
            )
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


package com.san.kir.manger.ui.application_navigation.accounts

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.features.shikimori.ui.catalog_item.ShikiItemScreen
import com.san.kir.features.shikimori.ui.catalog_item.ShikiItemViewModel
import com.san.kir.features.shikimori.ui.local_item.LocalItemScreen
import com.san.kir.features.shikimori.ui.local_item.LocalItemViewModel
import com.san.kir.features.shikimori.ui.main.ShikimoriScreen
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class AccountsNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            AccountsScreen(
                navigateUp = ::navigateUp,
                navigateToShiki = { navigate(Shikimori) },
            )
        }
    },
    Shikimori {
        override val content = navTarget(route = "shikimori") {
            ShikimoriScreen(hiltViewModel(),
                ::navigateUp,
                { navigate(ShikimoriItem, it) },
                { navigate(LocalItem, it) }
            )
        }
    },
    ShikimoriItem {
        override val content = navTarget(
            route = "shikimori_item",
            hasItem = true,
            arguments = listOf(navLongArgument()),
        ) {
            val viewModel = hiltViewModel<ShikiItemViewModel>()
            longElement?.let { viewModel.update(it) }

            ShikiItemScreen(
                viewModel,
                ::navigateUp
            ) { query -> navigate(CatalogsNavTarget.GlobalSearch, query) }
        }
    },
    LocalItem {
        override val content = navTarget(
            route = "local_item",
            hasItem = true,
            arguments = listOf(navLongArgument()),
        ) {
            val viewModel = hiltViewModel<LocalItemViewModel>()
            longElement?.let { viewModel.update(it) }

            LocalItemScreen(
                viewModel,
                ::navigateUp
            ) { query -> }
        }
    };
}

private val targets = AccountsNavTarget.values().toList()

fun NavGraphBuilder.accountsNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = AccountsNavTarget.Main,
        route = MainNavTarget.Accounts,
        targets = targets
    )
}

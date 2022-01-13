package com.san.kir.manger.ui.application_navigation.accounts

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.san.kir.features.shikimori.ui.catalog.ShikimoriScreen
import com.san.kir.features.shikimori.ui.catalog_item.ShikiItemScreen
import com.san.kir.features.shikimori.ui.catalog_item.ShikiItemViewModel
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.application_navigation.catalog.global_search.GlobalSearchScreen
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.getLongElement
import com.san.kir.manger.utils.compose.getStringElement
import com.san.kir.manger.utils.compose.navigate

sealed class AccountsNavTarget : NavTarget {
    object Main : AccountsNavTarget() {
        override val route: String = "main"
    }

    object Shikimori : AccountsNavTarget() {
        override val base: String = "accounts_shikimori"
    }

    object ShikimoriItem : AccountsNavTarget() {
        override val base = "accounts_shikimori_item"
        override val isOptional = true
    }

    object LocalItem : AccountsNavTarget() {
        override val base = "accounts_local_item"
        override val isOptional = true
    }

    object GlobalSearch : AccountsNavTarget() {
        override val base = "global_search"
        override val isOptional = true
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.accountsNavGraph(nav: NavHostController) {
    composable(
        route = AccountsNavTarget.Main.route,
        content = {
            AccountsScreen(
                navigateToUp = nav::navigateUp,
                navigateToShiki = { nav.navigate(AccountsNavTarget.Shikimori) },
            )
        }
    )

    composable(
        route = AccountsNavTarget.Shikimori.route(),
        content = {
            ShikimoriScreen(hiltViewModel(),
                nav::navigateUp,
                { nav.navigate(AccountsNavTarget.ShikimoriItem, it) },
                { nav.navigate(AccountsNavTarget.LocalItem, it) }
            )
        }
    )

    composable(
        route = AccountsNavTarget.ShikimoriItem.route,
        arguments = listOf(
            navArgument(AccountsNavTarget.ShikimoriItem.item.value) {
                type = NavType.LongType
            }),
        content = { back ->
            val id = back.getLongElement(AccountsNavTarget.ShikimoriItem)
            val viewModel = hiltViewModel<ShikiItemViewModel>()
            id?.let { viewModel.update(it) }

            ShikiItemScreen(
                viewModel,
                nav::navigateUp
            ) { query -> nav.navigate(AccountsNavTarget.GlobalSearch, query) }
        }
    )

    composable(
        route = AccountsNavTarget.LocalItem.route,
        arguments = listOf(
            navArgument(AccountsNavTarget.LocalItem.item.value) {
                type = NavType.LongType
            }),
        content = { back ->
            val id = back.getLongElement(AccountsNavTarget.LocalItem)


        }
    )

    composable(
        route = AccountsNavTarget.GlobalSearch.route,
        content = { back ->
            val initSearchText = back.getStringElement(CatalogsNavTarget.GlobalSearch) ?: ""

            GlobalSearchScreen(nav, initSearchText = initSearchText)
        }
    )
}

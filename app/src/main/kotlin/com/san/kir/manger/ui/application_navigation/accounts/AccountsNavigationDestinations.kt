package com.san.kir.manger.ui.application_navigation.accounts

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.features.shikimori.ui.accountRate.AccountRateScreen
import com.san.kir.features.shikimori.ui.accountScreen.AccountScreen
import com.san.kir.features.shikimori.ui.localItem.LocalItemScreen
import com.san.kir.features.shikimori.ui.localItems.LocalItemsScreen
import com.san.kir.features.shikimori.ui.search.ShikiSearchScreen
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class AccountsNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            val navigateTo: () -> Unit = remember { { navigate(Shikimori) } }
            AccountsScreen(
                navigateUp = up(),
                navigateToShiki = navigateTo,
            )
        }
    },
    Shikimori {
        override val content = navTarget(route = "shikimori") {
            val navigateToShiki: (Long) -> Unit = remember { { navigate(ProfileItem, it, -1L) } }
            val navigateToLocal: () -> Unit = remember { { navigate(LocalItems) } }
            val navigateToSearch: () -> Unit = remember { { navigate(Search) } }
            AccountScreen(
                up(),
                navigateToShikiItem = navigateToShiki,
                navigateToLocalItems = navigateToLocal,
                navigateToSearch = navigateToSearch
            )
        }
    },
    LocalItems {
        override val content = navTarget(route = "library_items") {
            val navigateTo: (Long) -> Unit = remember { { navigate(LocalItem, it) } }
            LocalItemsScreen(
                navigateUp = up(),
                navigateToItem = navigateTo
            )
        }
    },
    LocalItem {
        override val content = navTarget(
            route = "local_item",
            hasItems = true,
            arguments = listOf(navLongArgument()),
        ) {
            val navigateTo: (String) -> Unit = remember { { navigate(Search, it) } }
            LocalItemScreen(
                mangaId = longElement ?: -1L,
                navigateUp = up(),
                navigateToSearch = navigateTo
            )
        }
    },
    Search {
        override val content = navTarget(
            route = "shiki_search",
            hasItems = true,
        ) {
            val navigateTo: (Long) -> Unit = remember { { navigate(ProfileItem, it, -1L) } }
            ShikiSearchScreen(
                navigateUp = up(),
                navigateToItem = navigateTo,
                searchText = stringElement ?: "",
            )
        }
    },
    ProfileItem {
        private val mangaId = "shiki_profile_item_manga_id"
        private val rateId = "shiki_profile_item_rate_id"

        override val content = navTarget(
            route = "shiki_search_item",
            hasItems = true,
            arguments = listOf(navLongArgument(mangaId), navLongArgument(rateId))
        ) {
            val navigateTo: (String) -> Unit =
                remember { { navigate(CatalogsNavTarget.GlobalSearch, it) } }
            AccountRateScreen(
                navigateUp = up(),
                navigateToSearch = navigateTo,
                mangaId = longElement(mangaId) ?: -1L,
                rateId = longElement(rateId) ?: -1L,
            )
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

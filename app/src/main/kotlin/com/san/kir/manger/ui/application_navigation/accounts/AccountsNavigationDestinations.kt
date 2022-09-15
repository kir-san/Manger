package com.san.kir.manger.ui.application_navigation.accounts

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
import timber.log.Timber

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
            AccountScreen(
                ::navigateUp,
                navigateToShikiItem = { navigate(ProfileItem, it, -1L) },
                navigateToLocalItems = { navigate(LocalItems) },
                navigateToSearch = { navigate(Search) }
            )
        }
    },
    LocalItems {
        override val content = navTarget(route = "library_items") {
            LocalItemsScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { navigate(LocalItem, it) }
            )
        }
    },
    LocalItem {
        override val content = navTarget(
            route = "local_item",
            hasItems = true,
            arguments = listOf(navLongArgument()),
        ) {
            LocalItemScreen(
                mangaId = longElement ?: -1L,
                navigateUp = ::navigateUp,
                navigateToSearch = { query ->
                    Timber.v("query")
                    navigate(Search, query)
                }
            )
        }
    },
    Search {
        override val content = navTarget(
            route = "shiki_search",
            hasItems = true,
        ) {
            ShikiSearchScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { mangaId -> navigate(ProfileItem, mangaId, -1L) },
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
            AccountRateScreen(
                navigateUp = ::navigateUp,
                navigateToSearch = { query -> navigate(CatalogsNavTarget.GlobalSearch, query) },
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

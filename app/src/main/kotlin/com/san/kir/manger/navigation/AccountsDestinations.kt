package com.san.kir.manger.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.features.shikimori.ui.accountRate.AccountRateScreen
import com.san.kir.features.shikimori.ui.accountScreen.AccountScreen
import com.san.kir.features.shikimori.ui.localItem.LocalItemScreen
import com.san.kir.features.shikimori.ui.localItems.LocalItemsScreen
import com.san.kir.features.shikimori.ui.search.ShikiSearchScreen
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation
import com.san.kir.manger.ui.accounts.AccountsScreen

enum class AccountsNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Accounts.main) {
            AccountsScreen(
                navigateUp = navigateUp(),
                navigateToShiki = rememberNavigate(Shikimori),
            )
        }
    },
    Shikimori {
        override val content = navTarget(route = GraphTree.Accounts.shikimori) {
            AccountScreen(
                navigateUp(),
                navigateToShikiItem = rememberNavigateLong(ProfileItem),
                navigateToLocalItems = rememberNavigate(LocalItems),
                navigateToSearch = rememberNavigate(Search)
            )
        }
    },
    LocalItems {
        override val content = navTarget(route = GraphTree.Accounts.localItems) {
            LocalItemsScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateLong(LocalItem)
            )
        }
    },
    LocalItem {
        override val content = navTarget(
            route = GraphTree.Accounts.localItem,
            hasItems = true,
            arguments = listOf(navLongArgument()),
        ) {
            LocalItemScreen(
                mangaId = longElement() ?: -1L,
                navigateUp = navigateUp(),
                navigateToSearch = rememberNavigateString(Search)
            )
        }
    },
    Search {
        override val content = navTarget(
            route = GraphTree.Accounts.search,
            hasItems = true,
        ) {
            ShikiSearchScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateLong(ProfileItem),
                searchText = stringElement() ?: "",
            )
        }
    },
    ProfileItem {
        private val mangaId = "shiki_profile_item_manga_id"
        private val rateId = "shiki_profile_item_rate_id"

        override val content = navTarget(
            route = GraphTree.Accounts.shikiItem,
            hasItems = true,
            arguments = listOf(navLongArgument(mangaId), navLongArgument(rateId))
        ) {
            AccountRateScreen(
                navigateUp = navigateUp(),
                navigateToSearch = rememberNavigateString(CatalogsNavTarget.GlobalSearch),
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

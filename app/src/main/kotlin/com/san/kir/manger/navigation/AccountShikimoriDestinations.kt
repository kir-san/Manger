package com.san.kir.manger.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.features.shikimori.ui.accountRate.AccountRateScreen
import com.san.kir.features.shikimori.ui.accountScreen.AccountScreen
import com.san.kir.features.shikimori.ui.localItem.LocalItemScreen
import com.san.kir.features.shikimori.ui.localItems.LocalItemsScreen
import com.san.kir.features.shikimori.ui.search.ShikiSearchScreen
import com.san.kir.manger.navigation.utils.Constants
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation

enum class AccountShikimoriNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Accounts.Shikimori.main) {
            AccountScreen(
                navigateUp(),
                navigateToShikiItem = rememberNavigateLong(ProfileItem),
                navigateToLocalItems = rememberNavigate(LocalItems),
                navigateToSearch = rememberNavigate(Search)
            )
        }
    },
    LocalItems {
        override val content = navTarget(route = GraphTree.Accounts.Shikimori.localItems) {
            LocalItemsScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateLong(LocalItem)
            )
        }
    },
    LocalItem {
        override val content = navTarget(
            route = GraphTree.Accounts.Shikimori.localItem,
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
            route = GraphTree.Accounts.Shikimori.search,
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
            route = GraphTree.Accounts.Shikimori.shikiItem,
            hasItems = true,
            arguments = listOf(navLongArgument(mangaId) /*navLongArgument(rateId)*/)
        ) {
            AccountRateScreen(
                navigateUp = navigateUp(),
                navigateToSearch = rememberNavigateString(CatalogsNavTarget.GlobalSearch),
                mangaId = longElement(mangaId) ?: -1L,
                //                rateId = longElement(rateId) ?: -1L,
            )
        }
    };
}

private val targets = AccountShikimoriNavTarget.values().toList()

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.accountShikimoriNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = AccountShikimoriNavTarget.Main,
        route = AccountsNavTarget.Shikimori,
        targets = targets,
        enterTransition, exitTransition, popEnterTransition, popExitTransition
    )
}

@OptIn(ExperimentalAnimationApi::class)
private val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val initial = initialState.destination.route
    val target = targetState.destination.route

    if (initial == null || target == null) null
    else
        when {
            GraphTree.Accounts.Shikimori.search in target            ->
                scaleIn(
                    animationSpec = tween(Constants.duration),
                    initialScale = 0.08f,
                    transformOrigin = TransformOrigin(0.9f, 0.05f)
                )

            GraphTree.Accounts.Shikimori.localItems in target        ->
                scaleIn(
                    animationSpec = tween(Constants.duration),
                    initialScale = 0.08f,
                    transformOrigin = TransformOrigin(0.9f, 0.90f)
                )

            GraphTree.Accounts.Shikimori.localItem in target ||
                    GraphTree.Accounts.Shikimori.shikiItem in target ->
                expandVertically(
                    animationSpec = tween(Constants.duration),
                    expandFrom = Alignment.CenterVertically
                )

            else                                                     -> null
        }
}

@OptIn(ExperimentalAnimationApi::class)
private val exitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null) fadeOut(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popEnterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null) fadeIn(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val initial = initialState.destination.route
    val target = targetState.destination.route

    if (initial == null || target == null) null
    else
        when {
            GraphTree.Accounts.Shikimori.search in initial            ->
                scaleOut(
                    animationSpec = tween(Constants.duration),
                    transformOrigin = TransformOrigin(0.9f, 0.05f)
                )

            GraphTree.Accounts.Shikimori.localItems in initial        ->
                scaleOut(
                    animationSpec = tween(Constants.duration),
                    transformOrigin = TransformOrigin(0.9f, 0.90f)
                )

            GraphTree.Accounts.Shikimori.localItem in initial ||
                    GraphTree.Accounts.Shikimori.shikiItem in initial ->
                shrinkVertically(
                    animationSpec = tween(Constants.duration),
                    shrinkTowards = Alignment.CenterVertically
                )

            else                                                      -> null
        }
}

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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.catalog.ui.addStandart.AddStandartScreen
import com.san.kir.catalog.ui.catalog.CatalogScreen
import com.san.kir.catalog.ui.catalogItem.CatalogItemScreen
import com.san.kir.catalog.ui.catalogs.CatalogsScreen
import com.san.kir.catalog.ui.search.SearchScreen
import com.san.kir.manger.navigation.utils.Constants
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

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.catalogsNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = CatalogsNavTarget.Main,
        route = MainNavTarget.Catalogs,
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
            GraphTree.Catalogs.search in target   ->
                scaleIn(
                    animationSpec = tween(Constants.duration),
                    initialScale = 0.08f,
                    transformOrigin = TransformOrigin(0.9f, 0.05f)
                )

            GraphTree.Catalogs.item in target     ->
                expandVertically(
                    animationSpec = tween(Constants.duration),
                    expandFrom = Alignment.CenterVertically
                )

            GraphTree.Catalogs.itemInfo in target ->
                slideInVertically(
                    animationSpec = tween(Constants.duration),
                    initialOffsetY = { it }
                )

            GraphTree.Catalogs.itemAdd in target  ->
                scaleIn(
                    animationSpec = tween(Constants.duration),
                    initialScale = 0.1f,
                    transformOrigin = TransformOrigin(0.95f, 0.01f)
                )

            else                                  -> null
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
            GraphTree.Catalogs.search in initial   ->
                scaleOut(
                    animationSpec = tween(Constants.duration),
                    transformOrigin = TransformOrigin(0.9f, 0.05f)
                )

            GraphTree.Catalogs.item in initial     ->
                shrinkVertically(
                    animationSpec = tween(Constants.duration),
                    shrinkTowards = Alignment.CenterVertically
                )

            GraphTree.Catalogs.itemInfo in initial ->
                slideOutVertically(
                    animationSpec = tween(Constants.duration),
                    targetOffsetY = { it }
                )

            GraphTree.Catalogs.itemAdd in initial  ->
                scaleOut(
                    animationSpec = tween(Constants.duration),
                    transformOrigin = TransformOrigin(0.95f, 0.01f)
                )

            else                                   -> null
        }
}

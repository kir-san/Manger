package com.san.kir.manger.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.categories.ui.categories.CategoriesScreen
import com.san.kir.categories.ui.category.CategoryScreen
import com.san.kir.manger.navigation.utils.Constants
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation

enum class CategoriesNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Categories.main) {
            CategoriesScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateString(Category),
            )
        }
    },

    Category {
        override val content = navTarget(
            route = GraphTree.Categories.item,
            hasItems = true
        ) {
            CategoryScreen(
                navigateUp = navigateUp(),
                categoryName = stringElement() ?: "",
            )
        }
    }
}

private val targets = CategoriesNavTarget.values().toList()

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.categoriesNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = CategoriesNavTarget.Main,
        route = MainNavTarget.Categories,
        targets = targets,
        enterTransition, exitTransition, popEnterTransition, popExitTransition
    )
}

@OptIn(ExperimentalAnimationApi::class)
private val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null && GraphTree.Categories.main in target)
        expandIn(animationSpec = tween(Constants.duration), expandFrom = Alignment.Center)
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val exitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null && GraphTree.Categories.item in target)
        fadeOut(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popEnterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null && GraphTree.Categories.item in target)
        fadeIn(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null && GraphTree.Categories.main in target)
        shrinkOut(animationSpec = tween(Constants.duration), shrinkTowards = Alignment.Center)
    else null
}

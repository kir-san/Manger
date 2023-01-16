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
import com.san.kir.manger.navigation.utils.Constants
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation
import com.san.kir.statistic.ui.statistic.StatisticScreen
import com.san.kir.statistic.ui.statistics.StatisticsScreen

enum class StatisticNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Statistic.main) {
            StatisticsScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateLong(Statistic),
            )
        }
    },

    Statistic {
        override val content = navTarget(
            route = GraphTree.Statistic.item,
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            StatisticScreen(
                navigateUp = navigateUp(),
                itemId = longElement() ?: -1L
            )
        }
    };
}

private val targets = StatisticNavTarget.values().toList()

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.statisticNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = StatisticNavTarget.Main,
        route = MainNavTarget.Statistic,
        targets = targets,
        enterTransition, exitTransition, popEnterTransition, popExitTransition
    )
}

@OptIn(ExperimentalAnimationApi::class)
private val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null && GraphTree.Statistic.main in target)
        expandIn(animationSpec = tween(Constants.duration), expandFrom = Alignment.Center)
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val exitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null && GraphTree.Statistic.item in target)
        fadeOut(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popEnterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null && GraphTree.Statistic.item in target)
        fadeIn(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null && GraphTree.Statistic.main in target)
        shrinkOut(animationSpec = tween(Constants.duration), shrinkTowards = Alignment.Center)
    else null
}

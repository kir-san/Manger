package com.san.kir.manger.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
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

fun NavGraphBuilder.statisticNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = StatisticNavTarget.Main,
        route = MainNavTarget.Statistic,
        targets = targets
    )
}

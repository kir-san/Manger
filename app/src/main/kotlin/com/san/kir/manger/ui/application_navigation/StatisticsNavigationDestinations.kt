package com.san.kir.manger.ui.application_navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation
import com.san.kir.statistic.ui.statistic.StatisticScreen
import com.san.kir.statistic.ui.statistics.StatisticsScreen

enum class StatisticNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            StatisticsScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { navigate(Statistic, it) },
            )
        }
    },

    Statistic {
        override val content = navTarget(
            route = "statistic_item",
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            StatisticScreen(
                navigateUp = ::navigateUp,
                itemId = longElement ?: -1L
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

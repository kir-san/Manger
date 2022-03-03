package com.san.kir.manger.ui.application_navigation.statistic

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.statistic.main.StatisticsScreen
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

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
        override val content = navTarget(route = "statistic_item", hasItem = true) {
            val viewModel = onlyStatisticViewModel(stringElement ?: "")

            val statistic by viewModel.statistic.collectAsState()

            StatisticScreen(navigateUp = ::navigateUp, statistic)
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

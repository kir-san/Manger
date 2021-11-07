package com.san.kir.manger.ui.application_navigation.statistic

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.statistic.main.StatisticsScreen
import com.san.kir.manger.ui.utils.NavItem
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.StatisticItem
import com.san.kir.manger.ui.utils.getStringElement

sealed class StatisticNavTarget : NavTarget {
    object Main : StatisticNavTarget() {
        override val route: String = "main"
    }

    object Statistic : StatisticNavTarget() {
        override val base: String = "statistic_"
        override val item: NavItem = StatisticItem
        override val isOptional: Boolean = true
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.statisticNavGraph(nav: NavHostController) {
    composable(
        route = StatisticNavTarget.Main.route,
        content = {
            StatisticsScreen(nav)
        }
    )

    composable(
        route = StatisticNavTarget.Statistic.route,
        content = {
            val item = nav.getStringElement(StatisticItem) ?: ""
            val viewModel = onlyStatisticViewModel(mangaName = item)

            val statistic by viewModel.statistic.collectAsState()

            StatisticScreen(nav, statistic)
        }
    )
}

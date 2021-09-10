package com.san.kir.manger.ui.application_navigation.statistic

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.mangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.categories.CategoriesNavTarget
import com.san.kir.manger.ui.application_navigation.statistic.main.StatisticsScreen
import com.san.kir.manger.ui.application_navigation.storage.main.StorageScreen
import com.san.kir.manger.ui.utils.MangaItem
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.getElement

sealed class StatisticNavTarget : NavTarget {
    object Main : StatisticNavTarget() {
        override val route: String = "main"
    }

    object Statistic : StatisticNavTarget() {
        override val route: String = "statistic_"
        override val savedItem: String = route + "_item"
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
            val item = nav.getElement(StatisticNavTarget.Statistic) ?: MangaStatistic()

            StatisticScreen(nav, item)
        }
    )
}

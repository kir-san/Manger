package com.san.kir.manger.ui.application_navigation.schedule

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.schedule.main.SchedulesScreen
import com.san.kir.manger.ui.utils.NavItem
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.ScheduleItem
import com.san.kir.manger.ui.utils.getLongElement

sealed class ScheduleNavTarget : NavTarget {
    object Main : ScheduleNavTarget() {
        override val route: String = "main"
    }

    object Schedule : ScheduleNavTarget() {
        override val base: String = "schedule_"
        override val item: NavItem = ScheduleItem
        override val isOptional: Boolean = true
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.scheduleNavGraph(nav: NavHostController) {
    composable(
        route = ScheduleNavTarget.Main.route,
        content = {
            SchedulesScreen(nav)
        }
    )

    composable(
        route = ScheduleNavTarget.Schedule.route(),
        arguments = listOf(navArgument(ScheduleNavTarget.Schedule.item.value) {
            type = NavType.LongType
        }),
        content = {
            val item = nav.getLongElement(ScheduleItem) ?: -1L

            val viewModel = plannedTaskViewModel(item)

            PlannedTaskScreen(nav, viewModel)
        }
    )
}

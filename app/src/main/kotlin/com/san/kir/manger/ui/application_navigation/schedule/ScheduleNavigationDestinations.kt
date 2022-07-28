package com.san.kir.manger.ui.application_navigation.schedule

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.schedule.main.SchedulesScreen
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class ScheduleNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            SchedulesScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { navigate(Schedule, it) }
            )
        }
    },

    Schedule {
        override val content = navTarget(
            route = "schedule_item",
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            val viewModel = plannedTaskViewModel(longElement ?: -1L)

            PlannedTaskScreen(navigateUp = ::navigateUp, viewModel)
        }
    };
}

private val targets = ScheduleNavTarget.values().toList()

fun NavGraphBuilder.scheduleNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = ScheduleNavTarget.Main,
        route = MainNavTarget.Schedule,
        targets = targets
    )
}

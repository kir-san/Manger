package com.san.kir.manger.ui.application_navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation
import com.san.kir.schedule.ui.main.MainScreen
import com.san.kir.schedule.ui.task.TaskScreen

enum class ScheduleNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            val navigateTo: (Long) -> Unit = remember { { navigate(Schedule, it) } }

            MainScreen(
                navigateUp = up(),
                navigateToItem = navigateTo
            )
        }
    },

    Schedule {
        override val content = navTarget(
            route = "schedule_item",
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            TaskScreen(navigateUp = up(), longElement ?: -1L)
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

package com.san.kir.manger.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation
import com.san.kir.schedule.ui.main.MainScreen
import com.san.kir.schedule.ui.task.TaskScreen

enum class ScheduleNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Schedule.main) {
            MainScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateLong(Schedule)
            )
        }
    },

    Schedule {
        override val content = navTarget(
            route = GraphTree.Schedule.item,
            hasItems = true,
            arguments = listOf(navLongArgument())
        ) {
            TaskScreen(
                navigateUp = navigateUp(),
                itemId = longElement() ?: -1L
            )
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

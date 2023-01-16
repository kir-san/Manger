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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.navigation.utils.Constants
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navBoolArgument
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation
import com.san.kir.storage.ui.storage.StorageScreen
import com.san.kir.storage.ui.storages.StoragesScreen

enum class StorageNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Storage.main) {
            val navigateToItem = remember { { arg: Long -> navigate(Storage, arg, false) } }

            StoragesScreen(
                navigateUp = navigateUp(),
                navigateToItem = navigateToItem,
            )
        }
    },

    Storage {
        private val hasUpdate = "hasUpdate"

        override val content = navTarget(
            route = GraphTree.Storage.item,
            hasItems = true,
            arguments = listOf(navLongArgument(), navBoolArgument(hasUpdate))
        ) {
            StorageScreen(
                navigateUp = navigateUp(),
                mangaId = longElement() ?: -1L,
                hasUpdate = booleanElement(hasUpdate) ?: false
            )
        }
    }
}

private val targets = StorageNavTarget.values().toList()

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.storageNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = StorageNavTarget.Main,
        route = MainNavTarget.Storage,
        targets = targets,
        enterTransition, exitTransition, popEnterTransition, popExitTransition
    )
}

@OptIn(ExperimentalAnimationApi::class)
private val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null && GraphTree.Storage.main in target)
        expandIn(animationSpec = tween(Constants.duration), expandFrom = Alignment.Center)
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val exitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null && GraphTree.Storage.item in target)
        fadeOut(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popEnterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition? = {
    val target = initialState.destination.route
    if (target != null && GraphTree.Storage.item in target)
        fadeIn(animationSpec = tween(Constants.duration))
    else null
}

@OptIn(ExperimentalAnimationApi::class)
private val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition? = {
    val target = targetState.destination.route
    if (target != null && GraphTree.Storage.main in target)
        shrinkOut(animationSpec = tween(Constants.duration), shrinkTowards = Alignment.Center)
    else null
}

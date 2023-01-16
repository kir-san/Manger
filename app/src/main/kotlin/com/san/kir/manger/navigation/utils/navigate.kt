package com.san.kir.manger.navigation.utils

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.composable

// Ключ по которому передаются аргументы
internal const val defaultItemKey = "sended_item"

// Интерфейс для создания любой точки навигации
interface NavTarget {
    val content: NavTargetContent
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.composable(
    nav: NavHostController, target: NavTarget,
    enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
) {

    with(target.content) {
        composable(
            route = route(),
            content = { back ->
                ContentScopeImpl(nav, back).content()
            },
            arguments = arguments,
            deepLinks = deepLinks,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        )
    }
}

// Удобное создание вложенной навигации
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.navigation(
    nav: NavHostController,
    startDestination: NavTarget,
    route: NavTarget,
    targets: List<NavTarget>,
    enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    builder: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        startDestination = startDestination.content.route(),
        route = route.content.route(),
        builder = {
            targets.forEach { target ->
                composable(
                    nav, target,
                    enterTransition, exitTransition, popEnterTransition, popExitTransition
                )
            }
            builder()
        }
    )
}

fun NavHostController.navigate(target: NavTarget, vararg dest: Any = emptyArray()) {
    if (dest.isEmpty()) {
        navigate(target.content.route())
    } else {
        navigate(target.content.route(*dest))
    }
}

fun navLongArgument(customItemKey: String = defaultItemKey) =
    navArgument(customItemKey) { type = NavType.LongType }

fun navBoolArgument(customItemKey: String = defaultItemKey) =
    navArgument(customItemKey) { type = NavType.BoolType }

inline fun <reified T : ComponentActivity> Context.deepLinkIntent(target: NavTarget) = Intent(
    Intent.ACTION_VIEW,
    target.content.deepLink.toUri(),
    this,
    T::class.java
)

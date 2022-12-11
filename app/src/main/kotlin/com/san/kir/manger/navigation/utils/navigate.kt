package com.san.kir.manger.navigation.utils

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.net.toUri
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
fun NavGraphBuilder.composable(nav: NavHostController, target: NavTarget) {

    with(target.content) {
        composable(
            route = route(),
            content = { back ->
                ContentScopeImpl(nav, back).content()
            },
            arguments = arguments,
            deepLinks = deepLinks,
        )
    }
}

// Удобное создание вложенной навигации
fun NavGraphBuilder.navigation(
    nav: NavHostController,
    startDestination: NavTarget,
    route: NavTarget,
    targets: List<NavTarget>,
) {
    navigation(
        startDestination = startDestination.content.route(),
        route = route.content.route(),
        builder = {
            targets.forEach { target -> composable(nav, target) }
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

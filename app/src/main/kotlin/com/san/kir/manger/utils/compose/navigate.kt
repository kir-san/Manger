package com.san.kir.manger.utils.compose

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.composable

// Ключ по которому передаются аргументы
internal const val itemKey = "sended_item"

// Содержимое элемента навигации
interface NavTargetContent {

    val route: String
    val content: @Composable ContentScope.() -> Unit
    val deepLinks: List<NavDeepLink>
    val arguments: List<NamedNavArgument>

    // Принимает ли экран какие-либо параметры
    val hasItem: Boolean

    // Построитель шаблона в зависимости от найстроек экрана
    fun route(value: String = itemKey): String {
        // Флаг, является ли в данный момент передача параметра или шаблон для регистрации в контроллере
        val isTemplate = value == itemKey

        fun surround(value: String) = if (isTemplate) "{$value}" else value

        return if (hasItem)
            "$route?${itemKey}=${surround(value)}"
        else
            route
    }

    val deepLink: String
        get() = "android-app://androidx.navigation//$route"
}

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
                val scope = ContentScopeImpl(nav, back)
                scope.content()
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

// Простой доступ к управлению навигацией для экранов
interface ContentScope {
    fun navigateUp()
    fun navigate(target: NavTarget)
    fun navigate(target: NavTarget, dest: Any)
    val stringElement: String?
    val longElement: Long?
}

fun NavHostController.navigate(target: NavTarget, dest: Any? = null) {
    dest?.let { navigate(target.content.route(dest.toString())) }
        ?: navigate(target.content.route())
}

internal class ContentScopeImpl(
    private val nav: NavHostController,
    private val back: NavBackStackEntry,
) : ContentScope {
    override fun navigateUp() {
        nav.navigateUp()
    }

    override fun navigate(target: NavTarget) {
        nav.navigate(target)
    }

    override fun navigate(target: NavTarget, dest: Any) {
        nav.navigate(target, dest)
    }

    override val stringElement: String?
        get() = back.stringElement()


    override val longElement: Long?
        get() = back.longElement()

}

// Создание точки навигации
fun navTarget(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    hasItem: Boolean = false,
    hasDeepLink: Boolean = false,
    content: @Composable ContentScope.() -> Unit = {},
): NavTargetContent {
    return object : NavTargetContent {
        override val route: String = route
        override val content: @Composable ContentScope.() -> Unit =
            content
        override val deepLinks: List<NavDeepLink> =
            if (hasDeepLink) listOf(navDeepLink { uriPattern = deepLink }) else emptyList()
        override val arguments: List<NamedNavArgument> = arguments
        override val hasItem: Boolean = hasItem
    }
}

fun navLongArgument() = navArgument(itemKey) { type = NavType.LongType }

inline fun <reified T : ComponentActivity> Context.deepLinkIntent(target: NavTarget) = Intent(
    Intent.ACTION_VIEW,
    target.content.deepLink.toUri(),
    this,
    T::class.java
)

// Получение данных из аргументов

fun NavBackStackEntry.stringElement(): String? {
    return arguments?.getString(itemKey)
}

fun NavBackStackEntry.longElement(): Long? {
    return arguments?.getLong(itemKey)
}

package com.san.kir.manger.utils.compose

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import timber.log.Timber
import kotlin.reflect.KFunction0

// Ключ по которому передаются аргументы
internal const val itemKey = "sended_item"

// Содержимое элемента навигации
interface NavTargetContent {

    val route: String
    val content: @Composable ContentScope.() -> Unit
    val deepLinks: List<NavDeepLink>
    val arguments: List<NamedNavArgument>

    // Принимает ли экран какие-либо параметры
    val hasItems: Boolean

    // Построитель шаблона в зависимости от найстроек экрана
    fun route(vararg values: Any = arrayOf(itemKey)): String {

        // Флаг isTemplate, является ли в данный момент передача параметра или шаблон для регистрации в контроллере
        fun surround(isTemplate: Boolean, value: String): String {
            return if (isTemplate) "{$value}" else value
        }

        val buildString = buildString {
            append(route)

            if (hasItems) {
                if (arguments.isNotEmpty())
                    arguments.forEachIndexed { index, argument ->
                        var gettedValue = (values.getOrNull(index) ?: argument.name).toString()
                        if (gettedValue == itemKey) gettedValue = argument.name

                        val surround = surround(gettedValue == argument.name, gettedValue)

                        Timber.d("gettedValue is $gettedValue")
                        Timber.d("surround is $surround")

                        if (index == 0) append("?")
                        else append("&")

                        append("${argument.name}=$surround")
                    }
                else
                    values.forEachIndexed { index, value ->
                        val surround = surround(value.toString() == itemKey, value.toString())

                        if (index == 0) append("?")
                        else append("&")

                        append("${itemKey}=$surround")
                    }
            }
        }

        Timber.d(buildString)
        return buildString
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
    fun navigate(target: NavTarget, vararg dest: Any)

    @Composable
    fun longElement(itemKey: String): Long?

    @Composable
    fun stringElement(itemKey: String): String?

    @Composable
    fun booleanElement(itemKey: String): Boolean?
    val stringElement: String?
    val longElement: Long?
    val booleanElement: Boolean?

    @Composable
    fun up(): KFunction0<Unit> = remember { ::navigateUp }
}

fun NavHostController.navigate(target: NavTarget, vararg dest: Any = emptyArray()) {
    if (dest.isEmpty()) {
        navigate(target.content.route())
    } else {
        navigate(target.content.route(*dest))
    }
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

    override fun navigate(target: NavTarget, vararg dest: Any) {
        nav.navigate(target, *dest)
    }

    @Composable
    override fun longElement(itemKey: String): Long? = remember { back.longElement(itemKey) }

    @Composable
    override fun stringElement(itemKey: String): String? = remember { back.stringElement(itemKey) }

    @Composable
    override fun booleanElement(itemKey: String): Boolean? = remember { back.boolElement(itemKey) }

    override val stringElement: String?
        get() = back.stringElement()


    override val longElement: Long?
        get() = back.longElement()

    override val booleanElement: Boolean?
        get() = back.boolElement()
}

// Создание точки навигации
fun navTarget(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    hasItems: Boolean = false,
    hasDeepLink: Boolean = false,
    content: @Composable ContentScope.() -> Unit = {},
): NavTargetContent {
    return object : NavTargetContent {
        override val route: String = route
        override val content: @Composable ContentScope.() -> Unit = content
        override val deepLinks: List<NavDeepLink> =
            if (hasDeepLink) listOf(navDeepLink { uriPattern = deepLink }) else emptyList()
        override val arguments: List<NamedNavArgument> = arguments
        override val hasItems: Boolean = hasItems
    }
}

fun navLongArgument(customItemKey: String = itemKey) =
    navArgument(customItemKey) { type = NavType.LongType }

fun navBoolArgument(customItemKey: String = itemKey) =
    navArgument(customItemKey) { type = NavType.BoolType }

inline fun <reified T : ComponentActivity> Context.deepLinkIntent(target: NavTarget) = Intent(
    Intent.ACTION_VIEW,
    target.content.deepLink.toUri(),
    this,
    T::class.java
)

// Получение данных из аргументов

fun NavBackStackEntry.stringElement(customItemKey: String = itemKey): String? {
    return arguments?.getString(customItemKey)
}

fun NavBackStackEntry.longElement(customItemKey: String = itemKey): Long? {
    return arguments?.getLong(customItemKey)
}

fun NavBackStackEntry.boolElement(customItemKey: String = itemKey): Boolean? {
    return arguments?.getBoolean(customItemKey)
}

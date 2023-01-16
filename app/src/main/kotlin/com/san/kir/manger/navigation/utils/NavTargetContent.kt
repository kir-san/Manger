package com.san.kir.manger.navigation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import timber.log.Timber

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

// Содержимое элемента навигации
interface NavTargetContent {

    val route: String
    val content: @Composable ContentScope.() -> Unit
    val deepLinks: List<NavDeepLink>
    val arguments: List<NamedNavArgument>

    // Принимает ли экран какие-либо параметры
    val hasItems: Boolean

    // Построитель шаблона в зависимости от найстроек экрана
    fun route(vararg values: Any = arrayOf(defaultItemKey)): String {

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
                        if (gettedValue == defaultItemKey) gettedValue = argument.name

                        val surround = surround(gettedValue == argument.name, gettedValue)

//                        Timber.d("gettedValue is $gettedValue")
//                        Timber.d("surround is $surround")

                        if (index == 0) append("?")
                        else append("&")

                        append("${argument.name}=$surround")
                    }
                else
                    values.forEachIndexed { index, value ->
                        val surround =
                            surround(value.toString() == defaultItemKey, value.toString())

                        if (index == 0) append("?")
                        else append("&")

                        append("$defaultItemKey=$surround")
                    }
            }
        }

        Timber.d(buildString)
        return buildString
    }

    val deepLink: String
        get() = "android-app://androidx.navigation//$route"
}

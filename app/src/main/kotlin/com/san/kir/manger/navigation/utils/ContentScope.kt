package com.san.kir.manger.navigation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

// Простой доступ к управлению навигацией для экранов
interface ContentScope {

    @Composable
    fun navigateUp(): () -> Boolean

    fun navigate(target: NavTarget, vararg dest: Any)

    @Composable
    fun longElement(itemKey: String): Long?

    @Composable
    fun stringElement(itemKey: String): String?

    @Composable
    fun booleanElement(itemKey: String): Boolean?

    @Composable
    fun longElement() = longElement(defaultItemKey)

    @Composable
    fun stringElement() = stringElement(defaultItemKey)

    @Composable
    fun booleanElement() = booleanElement(defaultItemKey)

    @Composable
    fun rememberNavigate(target: NavTarget): () -> Unit =
        remember { { navigate(target, dest = emptyArray()) } }

    @Composable
    fun rememberNavigateString(target: NavTarget): (String) -> Unit =
        remember { { arg: String -> navigate(target, arg) } }

    @Composable
    fun rememberNavigateLong(target: NavTarget): (Long) -> Unit =
        remember { { arg: Long -> navigate(target, arg) } }
}

internal class ContentScopeImpl(
    private val nav: NavHostController,
    private val back: NavBackStackEntry,
) : ContentScope {

    @Composable
    override fun navigateUp() =
        remember { nav::navigateUp }

    override fun navigate(target: NavTarget, vararg dest: Any) = nav.navigate(target, *dest)

    @Composable
    override fun longElement(itemKey: String) = remember { back.arguments?.getLong(itemKey) }

    @Composable
    override fun stringElement(itemKey: String) = remember { back.arguments?.getString(itemKey) }

    @Composable
    override fun booleanElement(itemKey: String) = remember { back.arguments?.getBoolean(itemKey) }
}

package com.san.kir.manger.utils.compose

import androidx.navigation.NavHostController

fun NavHostController.navigate(target: NavTarget) {
    navigate(target.route)
}

fun NavHostController.navigate(target: NavTarget, dest: Any) {
    navigate(target.route(dest.toString()))
}

fun NavHostController.getStringElement(target: NavItem): String? {
    return currentBackStackEntry
        ?.arguments
        ?.getString(target.value)
}

fun NavHostController.getLongElement(target: NavItem): Long? {
    return currentBackStackEntry
        ?.arguments
        ?.getLong(target.value)
}

interface NavTarget {
    val base: String
        get() = ""
    val item: NavItem
        get() = EmptyItem
    val isOptional: Boolean
        get() = false

    val route: String
        get() = route()

    val deepLink: String
        get() = "android-app://androidx.navigation//$route"

    fun route(value: String = item.value): String {
        val isTemplate = value == item.value

        fun surround(value: String) = if (isTemplate) "{$value}" else value

        return if (isOptional)
            "$base?${item.value}=${surround(value)}"
        else
            "$base/${surround(value)}"
    }
}

sealed class NavItem(val value: String)

object EmptyItem : NavItem(value = "")
object MangaItem : NavItem(value = "manga")
object SiteItem : NavItem(value = "site")
object SiteCatalogItem : NavItem(value = "catalog_item")
object CategoryItem : NavItem(value = "category")
object StatisticItem : NavItem(value = "statistic")
object ScheduleItem : NavItem(value = "schedule")

package com.san.kir.manger.ui.utils

import androidx.navigation.NavHostController
import com.san.kir.manger.utils.extensions.log

fun NavHostController.navigate(target: NavTarget) {
    navigate(target.route)
}

fun NavHostController.navigate(target: NavTarget, dest: Any) {
    navigate(target.route(dest.toString()))
}

fun NavHostController.getElement(target: NavItem): String? {
    return currentBackStackEntry
        ?.arguments
        ?.getString(target.value)
}

fun NavHostController.getLongElement(target: NavItem): Long? {
    return currentBackStackEntry
        ?.arguments
        ?.getLong(target.value)
}

fun NavHostController.printCurrentDestination() {
    log("${currentDestination?.route}")
    log("${currentDestination?.parent?.route}")
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

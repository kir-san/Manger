package com.san.kir.manger.ui.utils

import androidx.navigation.NavHostController
import com.san.kir.manger.utils.extensions.log

fun NavHostController.navigate(target: NavTarget) {
    navigate(target.route)
}

fun NavHostController.navigate(target: NavTarget, dest: String) {
    navigate("${target.base}/$dest")
}

fun NavHostController.getElement(target: NavItem): String? {
    return currentBackStackEntry
        ?.arguments
        ?.getString(target.value)
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

    val route: String
        get() = "$base/{${item.value}}"
}

sealed class NavItem(val value: String)

object EmptyItem : NavItem(value = "")
object MangaItem : NavItem(value = "manga")
object SiteItem : NavItem(value = "site")
object SiteCatalogItem : NavItem(value = "catalog_item")
object CategoryItem : NavItem(value = "category")
object StatisticItem : NavItem(value = "statistic")

package com.san.kir.manger.ui.utils

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import com.san.kir.manger.utils.extensions.log

fun NavHostController.navigate(target: NavTarget) {
    navigate(target.route)
}

fun NavHostController.navigate(target: NavTarget, value: Parcelable) {
    currentBackStackEntry?.replaceArguments(
        bundleOf(target.savedItem to value)
    )
    navigate(target.route)
}

fun NavHostController.navigate(target: NavTarget, dest: String) {
   navigate(target.base + dest)
}

fun <T : Parcelable> NavHostController.getElement(target: NavTarget): T? {
    return previousBackStackEntry
        ?.arguments
        ?.getParcelable(target.savedItem)
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
    val route: String
    val savedItem: String
        get() = ""
}

sealed class NavItem(val value: String)

object MangaItem : NavItem(value = "manga")
object SiteItem : NavItem(value = "site")

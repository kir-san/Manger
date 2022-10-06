package com.san.kir.manger.ui.application_navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navBoolArgument
import com.san.kir.manger.utils.compose.navLongArgument
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation
import com.san.kir.storage.ui.storage.StorageScreen
import com.san.kir.storage.ui.storages.StoragesScreen

enum class StorageNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            StoragesScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { navigate(Storage, it, false) },
            )
        }
    },

    Storage {
        private val hasUpdate = "hasUpdate"

        override val content = navTarget(
            route = "storage_item",
            hasItems = true,
            arguments = listOf(navLongArgument(), navBoolArgument(hasUpdate))
        ) {
            StorageScreen(
                navigateUp = ::navigateUp,
                mangaId = longElement ?: -1L,
                hasUpdate = booleanElement(hasUpdate) ?: false
            )
        }
    }
}

private val targets = StorageNavTarget.values().toList()

fun NavGraphBuilder.storageNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = StorageNavTarget.Main,
        route = MainNavTarget.Storage,
        targets = targets
    )
}

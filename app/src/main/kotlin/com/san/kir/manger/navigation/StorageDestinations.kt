package com.san.kir.manger.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navBoolArgument
import com.san.kir.manger.navigation.utils.navLongArgument
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation
import com.san.kir.storage.ui.storage.StorageScreen
import com.san.kir.storage.ui.storages.StoragesScreen

enum class StorageNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Storage.main) {
            StoragesScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateLong(Storage),
            )
        }
    },

    Storage {
        private val hasUpdate = "hasUpdate"

        override val content = navTarget(
            route = GraphTree.Storage.item,
            hasItems = true,
            arguments = listOf(navLongArgument(), navBoolArgument(hasUpdate))
        ) {
            StorageScreen(
                navigateUp = navigateUp(),
                mangaId = longElement() ?: -1L,
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

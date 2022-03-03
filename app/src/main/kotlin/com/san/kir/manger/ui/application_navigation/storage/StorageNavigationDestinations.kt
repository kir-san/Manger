package com.san.kir.manger.ui.application_navigation.storage

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.mangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.storage.main.StorageScreen
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class StorageNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            StorageScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { navigate(Storage, it) },
            )
        }
    },

    Storage {
        override val content = navTarget(route = "storage_item", hasItem = true) {
            val viewModel = mangaStorageViewModel(stringElement ?: "")

            MangaStorageScreen(navigateUp = ::navigateUp, viewModel)
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

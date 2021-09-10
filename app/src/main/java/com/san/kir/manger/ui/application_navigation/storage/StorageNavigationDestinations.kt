package com.san.kir.manger.ui.application_navigation.storage

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.mangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.storage.main.StorageScreen
import com.san.kir.manger.ui.utils.MangaItem
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.getElement

sealed class StorageNavTarget : NavTarget {
    object Main : StorageNavTarget() {
        override val route: String = "main"
    }

    object Storage : StorageNavTarget() {
        override val base: String = "manga_storage/"
        override val route: String = "$base{${MangaItem.value}}"
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.storageNavGraph(nav: NavHostController) {
    composable(
        route = StorageNavTarget.Main.route,
        content = {
            StorageScreen(nav)
        }
    )

    composable(
        route = StorageNavTarget.Storage.route,
        content = {
            val item = nav.getElement(MangaItem) ?: ""
            val viewModel = mangaStorageViewModel(item)

            MangaStorageScreen(nav, viewModel)
        }
    )
}

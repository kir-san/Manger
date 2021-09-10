package com.san.kir.manger.ui.application_navigation.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAboutScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddOnlineScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaEditScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.mangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.library.chapters.ChaptersScreen
import com.san.kir.manger.ui.application_navigation.library.chapters.chaptersViewModel
import com.san.kir.manger.ui.application_navigation.library.main.LibraryScreen
import com.san.kir.manger.ui.utils.MangaItem
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.getElement

sealed class LibraryNavTarget : NavTarget {
    object Main : LibraryNavTarget() {
        override val route: String = "main"
    }

    object Chapters : LibraryNavTarget() {
        override val base: String = "chapters/"
        override val route: String = "$base{${MangaItem.value}}"
    }

    object AddOnline : LibraryNavTarget() {
        override val route: String = "add_online"
    }

    object AddLocal : LibraryNavTarget() {
        override val route: String = "add_local"
        override val savedItem: String = route + "_item"
    }

    object About : LibraryNavTarget() {
        override val route: String = "about"
        override val savedItem: String = route + "_item"
    }

    object Edit : LibraryNavTarget() {
        override val route: String = "edit"
        override val savedItem: String = route + "_item"
    }

    object Storage : LibraryNavTarget() {
        override val base: String = "manga_storage/"
        override val route: String = "$base{${MangaItem.value}}"
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.libraryNavGraph(nav: NavHostController) {
    composable(
        route = LibraryNavTarget.Main.route,
        content = {
            LibraryScreen(nav)
        }
    )

    composable(
        route = LibraryNavTarget.Chapters.route,
        content = {
            val item = nav.getElement(MangaItem) ?: ""
            val viewModel = chaptersViewModel(item)

            ChaptersScreen(nav, viewModel)
        }
    )

    composable(
        route = LibraryNavTarget.AddOnline.route,
        content = {
            MangaAddOnlineScreen(nav)
        }
    )

    composable(
        route = LibraryNavTarget.AddLocal.route,
        content = {
            val item = nav.getElement(LibraryNavTarget.AddLocal) ?: SiteCatalogElement()

            MangaAddScreen(nav, item)
        }
    )

    composable(
        route = LibraryNavTarget.About.route,
        content = {
            val item = nav.getElement(LibraryNavTarget.About) ?: Manga()

            MangaAboutScreen(nav, item)
        }
    )

    composable(
        route = LibraryNavTarget.Edit.route,
        content = {
            val item = nav.getElement(LibraryNavTarget.Edit) ?: Manga()

            MangaEditScreen(nav, item)
        }
    )

    composable(
        route = LibraryNavTarget.Storage.route,
        content = {
            val item = nav.getElement(MangaItem) ?: ""
            val viewModel = mangaStorageViewModel(item)

            MangaStorageScreen(nav, viewModel)
        }
    )
}

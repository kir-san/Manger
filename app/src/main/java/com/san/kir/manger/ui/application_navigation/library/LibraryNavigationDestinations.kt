package com.san.kir.manger.ui.application_navigation.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAboutScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddOnlineScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaEditScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.mangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.library.chapters.ChaptersScreen
import com.san.kir.manger.ui.application_navigation.library.chapters.chaptersViewModel
import com.san.kir.manger.ui.application_navigation.library.main.LibraryScreen
import com.san.kir.manger.ui.onlyMangaViewModel
import com.san.kir.manger.ui.utils.MangaItem
import com.san.kir.manger.ui.utils.NavItem
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.SiteCatalogItem
import com.san.kir.manger.ui.utils.getStringElement

sealed class LibraryNavTarget : NavTarget {
    object Main : LibraryNavTarget() {
        override val base: String = "main"
    }

    object Chapters : LibraryNavTarget() {
        override val base: String = "chapters"
        override val item: NavItem = MangaItem
        override val isOptional: Boolean = true
    }

    object AddOnline : LibraryNavTarget() {
        override val base: String = "add_online"
    }

    object AddLocal : LibraryNavTarget() {
        override val base: String = "add_local"
        override val item: NavItem = SiteCatalogItem
        override val isOptional: Boolean = true
    }

    object About : LibraryNavTarget() {
        override val base: String = "about"
        override val item: NavItem = MangaItem
        override val isOptional: Boolean = true
    }

    object Edit : LibraryNavTarget() {
        override val base: String = "edit"
        override val item: NavItem = MangaItem
        override val isOptional: Boolean = true
    }

    object Storage : LibraryNavTarget() {
        override val base: String = "manga_storage"
        override val item: NavItem = MangaItem
        override val isOptional: Boolean = true
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
            val item = nav.getStringElement(MangaItem) ?: ""
            val viewModel = chaptersViewModel(item)

            ChaptersScreen(viewModel, nav::navigateUp)
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
            val item = nav.getStringElement(SiteCatalogItem) ?: ""

            MangaAddScreen(item, nav::navigateUp)
        }
    )

    composable(
        route = LibraryNavTarget.About.route,
        content = {
            val item = nav.getStringElement(MangaItem) ?: ""
            val viewModel = onlyMangaViewModel(mangaUnic = item)

            val manga by viewModel.manga.collectAsState()

            MangaAboutScreen(nav, manga)
        }
    )

    composable(
        route = LibraryNavTarget.Edit.route,
        content = {
            val item = nav.getStringElement(MangaItem) ?: ""
            val viewModel = onlyMangaViewModel(mangaUnic = item)

            val manga by viewModel.manga.collectAsState()

            MangaEditScreen(nav, manga)
        }
    )

    composable(
        route = LibraryNavTarget.Storage.route,
        content = {
            val item = nav.getStringElement(MangaItem) ?: ""
            val viewModel = mangaStorageViewModel(item)

            MangaStorageScreen(nav, viewModel)
        }
    )
}

package com.san.kir.manger.ui.application_navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import com.san.kir.manger.ui.application_navigation.ApplicationNavigationDestination.Drawer
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.AboutMangaScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.AddMangaOnlineScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.AddMangaScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaInfoScreen
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.StorageMangaScreen
import com.san.kir.manger.ui.application_navigation.catalog.CatalogScreen
import com.san.kir.manger.ui.application_navigation.chapters.ChaptersScreen
import com.san.kir.manger.ui.application_navigation.drawer.DrawerScreen
import com.san.kir.manger.ui.application_navigation.drawer.categories.CategoryEditScreen
import com.san.kir.manger.ui.application_navigation.global_search.GlobalSearchScreen

enum class ApplicationNavigationDestination(
    val route: String,
    val element: String = "",
    val arguments: List<NamedNavArgument> = emptyList(),
    val content: @Composable (navController: NavHostController, close: () -> Unit) -> Unit
) {
    // BIG SCREENS //
    // Переключение по всем пунктам меню
    Drawer(
        route = "drawer",
        content = { nav, close -> DrawerScreen(close, nav) }
    ),

    // Глобальный поиск манги по доступным сайтам
    CatalogsSearch(
        route = "global_search",
        content = { nav, _ -> GlobalSearchScreen(nav) }
    ),

    // Каталог манги для выбраного сайта
    Catalog(
        route = "catalog",
        element = "catalog_element",
        content = { nav, _ -> CatalogScreen(nav) }
    ),

    // Экран глав манги
    Chapters(
        route = "list_chapters",
        element = "list_chapter_element",
        content = { nav, _ -> ChaptersScreen(nav) }
    ),


    // MINI SCREENS //
    // Добавление манги по ссылке
    AddMangaOnline(
        route = "add_manga_online",
        content = { nav, _ -> AddMangaOnlineScreen(nav) }
    ),

    // Просмотр всей информации на сайте о манге
    MangaInfo(
        route = "manga_info",
        element = "element",
        content = { nav, _ -> MangaInfoScreen(nav) }
    ),

    // Просмотр информации о манге
    AboutManga(
        route = "about_manga",
        element = "manga_element",
        content = { nav, _ -> AboutMangaScreen(nav) }
    ),

    // Добавление манги с выбором категории
    AddManga(
        route = "add_manga",
        element = "add_manga_element",
        content = { nav, _ -> AddMangaScreen(nav) }
    ),

    // Редактирование манги
    EditManga(
        route = "edit_manga",
        element = "edit_manga_element",
        content = { nav, _ -> }
    ),

    // Просмотр информации о занимаемом объеме
    StorageManga(
        route = "storage_manga",
        element = "storage_manga_element",
        content = { nav, _ -> StorageMangaScreen(nav) }
    ),

    // Редактирование параметров категории
    EditCategory(
        route = "edit_category",
        element = "edit_category_element",
        content = { nav, _ -> CategoryEditScreen(nav) }
    )
}

fun NavHostController.applicationGraph(close: () -> Unit) =
    createGraph(Drawer.route, null) {
        ApplicationNavigationDestination.values().forEach { screen ->
            composable(
                route = screen.route,
                arguments = screen.arguments,
                content = { screen.content(this@applicationGraph, close) })
        }
    }


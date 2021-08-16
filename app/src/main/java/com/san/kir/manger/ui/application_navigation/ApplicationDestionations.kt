package com.san.kir.manger.ui.application_navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
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

sealed class ApplicationNavigationDestination(
    val route: String,
    val element: String = "",
    val arguments: List<NamedNavArgument> = emptyList(),
    val content: @Composable (navController: NavHostController, close: () -> Unit) -> Unit
)

val MAIN_ALL_SCREENS =
    listOf(
        DrawerNavigationDestination,
        CatalogsSearchNavigationDestination,
        CatalogNavigationDestination(),
        ChaptersNavigationDestination,

        AddMangaOnlineNavigationDestination,
        MangaInfoNavigationDestination,
        AddMangaNavigationDestination,
        AboutMangaNavigationDestination,
        EditManga,
        StorageMangaNavigationDestination,
        EditCategoryNavigationDestination
    )

// Переключение по всем пунктам меню
object DrawerNavigationDestination : ApplicationNavigationDestination(
    route = "drawer",
    content = { nav, close -> DrawerScreen(close, nav) }
)

// Глобальный поиск манги по доступным сайтам
object CatalogsSearchNavigationDestination : ApplicationNavigationDestination(
    route = "global_search",
    content = { nav, _ -> GlobalSearchScreen(nav) }
)

// Каталог манги для выбраного сайта
class CatalogNavigationDestination(val siteName: String = "siteName", val base: String = "catalog") : ApplicationNavigationDestination(
    route = "$base/{$siteName}",
    arguments = listOf(navArgument(siteName) { type = NavType.StringType }),
    content = { nav, _ -> CatalogScreen(nav) }
)

// Экран глав манги
object ChaptersNavigationDestination : ApplicationNavigationDestination(
    route = "list_chapters",
    element = "list_chapter_element",
    content = { nav, _ -> ChaptersScreen(nav)}
)

// Добавление манги по ссылке
object AddMangaOnlineNavigationDestination : ApplicationNavigationDestination(
    route = "add_manga_online",
    content = { nav, _ -> AddMangaOnlineScreen(nav) }
)

// Просмотр всей информации на сайте о манге
object MangaInfoNavigationDestination : ApplicationNavigationDestination(
    route = "manga_info",
    element = "element",
    content = { nav, _ -> MangaInfoScreen(nav) }
)

// Просмотр информации о манге
object AboutMangaNavigationDestination : ApplicationNavigationDestination(
    route = "about_manga",
    element = "manga_element",
    content = { nav, _ -> AboutMangaScreen(nav) }
)

// Добавление манги с выбором категории
object AddMangaNavigationDestination : ApplicationNavigationDestination(
    route = "add_manga",
    element = "add_manga_element",
    content = { nav, _ -> AddMangaScreen(nav) }
)

// Редактирование манги
object EditManga : ApplicationNavigationDestination(
    route = "edit_manga",
    element = "edit_manga_element",
    content = { nav, _ -> }
)

// Просмотр информации о занимаемом объеме
object StorageMangaNavigationDestination : ApplicationNavigationDestination(
    route = "storage_manga",
    element = "storage_manga_element",
    content = { nav, _ -> StorageMangaScreen(nav) }
)

// Редактирование параметров категории
object EditCategoryNavigationDestination : ApplicationNavigationDestination(
    route = "edit_category",
    element = "edit_category_element",
    content = { nav, _ -> CategoryEditScreen(nav) }
)

package com.san.kir.manger.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.ui.catalog.CatalogScreen
import com.san.kir.manger.ui.drawer.DrawerScreen
import com.san.kir.manger.ui.drawer.categories.CategoryEditScreen
import com.san.kir.manger.ui.global_search.GlobalSearchScreen
import com.san.kir.manger.ui.manga_screens.AboutMangaScreen
import com.san.kir.manger.ui.manga_screens.AddMangaOnlineScreen
import com.san.kir.manger.ui.manga_screens.AddMangaScreen
import com.san.kir.manger.ui.manga_screens.MangaInfoScreen
import com.san.kir.manger.ui.storage.StorageMangaScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed class MainAppScreen(
    val route: String,
    val element: String = "",
    val arguments: List<NamedNavArgument> = emptyList(),
    val content: @Composable (navController: NavHostController, close: () -> Unit) -> Unit
)

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
val MAIN_ALL_SCREENS =
    listOf(
        Drawer,
        CatalogsSearch,
        Catalog(),
        ListChapters,

        AddMangaOnline,
        MangaInfo,
        AddManga,
        AboutManga,
        EditManga,
        StorageManga,
        EditCategory
    )

// Переключение по всем пунктам меню
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
object Drawer : MainAppScreen(
    route = "drawer",
    content = { nav, close -> DrawerScreen(close, nav) }
)

// Глобальный поиск манги по доступным сайтам
@ExperimentalAnimationApi
object CatalogsSearch : MainAppScreen(
    route = "global_search",
    content = { nav, _ -> GlobalSearchScreen(nav) }
)

// Каталог манги для выбраного сайта
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
class Catalog(val siteName: String = "siteName", val base: String = "catalog") : MainAppScreen(
    route = "$base/{$siteName}",
    arguments = listOf(navArgument(siteName) { type = NavType.StringType }),
    content = { nav, _ -> CatalogScreen(nav) }
)

// Экран глав манги
object ListChapters : MainAppScreen(
    route = "list_chapters",
    element = "list_chapter_element",
    content = { _, _ -> }
)

// Добавление манги по ссылке
@ExperimentalAnimationApi
object AddMangaOnline : MainAppScreen(
    route = "add_manga_online",
    content = { nav, _ -> AddMangaOnlineScreen(nav) }
)

// Просмотр всей информации на сайте о манге
@ExperimentalAnimationApi
object MangaInfo : MainAppScreen(
    route = "manga_info",
    element = "element",
    content = { nav, _ -> MangaInfoScreen(nav) }
)

// Просмотр информации о манге
@ExperimentalAnimationApi
object AboutManga : MainAppScreen(
    route = "about_manga",
    element = "manga_element",
    content = { nav, _ -> AboutMangaScreen(nav) }
)

// Добавление манги с выбором категории
@ExperimentalAnimationApi
object AddManga : MainAppScreen(
    route = "add_manga",
    element = "add_manga_element",
    content = { nav, _ -> AddMangaScreen(nav) }
)

// Редактирование манги
object EditManga : MainAppScreen(
    route = "edit_manga",
    element = "edit_manga_element",
    content = { nav, _ -> }
)

// Просмотр информации о занимаемом объеме
@ExperimentalAnimationApi
object StorageManga : MainAppScreen(
    route = "storage_manga",
    element = "storage_manga_element",
    content = { nav, _ -> StorageMangaScreen(nav) }
)

@ExperimentalAnimationApi
object EditCategory : MainAppScreen(
    route = "edit_category",
    element = "edit_category_element",
    content = { nav, _ -> CategoryEditScreen(nav) }
)

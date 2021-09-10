package com.san.kir.manger.ui.application_navigation.categories

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaAddScreen
import com.san.kir.manger.ui.application_navigation.categories.category.CategoryEditScreen
import com.san.kir.manger.ui.application_navigation.categories.main.CategoriesScreen
import com.san.kir.manger.ui.application_navigation.library.main.LibraryScreen
import com.san.kir.manger.ui.utils.NavTarget
import com.san.kir.manger.ui.utils.getElement

sealed class CategoriesNavTarget : NavTarget {
    object Main : CategoriesNavTarget() {
        override val route: String = "main"
    }

    object Category : CategoriesNavTarget() {
        override val route: String = "category"
        override val savedItem: String = route + "_item"
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.categoriesNavGraph(nav: NavHostController) {
    composable(
        route = CategoriesNavTarget.Main.route,
        content = {
            CategoriesScreen(nav)
        }
    )

    composable(
        route = CategoriesNavTarget.Category.route,
        content = {
            val item = nav.getElement(CategoriesNavTarget.Category) ?: Category()

            CategoryEditScreen(nav, item)
        }
    )
}

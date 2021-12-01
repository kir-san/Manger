package com.san.kir.manger.ui.application_navigation.categories

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.san.kir.manger.ui.application_navigation.categories.category.CategoryEditScreen
import com.san.kir.manger.ui.application_navigation.categories.category.CategoryEditViewModel
import com.san.kir.manger.ui.application_navigation.categories.main.CategoriesScreen
import com.san.kir.manger.utils.compose.CategoryItem
import com.san.kir.manger.utils.compose.NavItem
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.getStringElement

sealed class CategoriesNavTarget : NavTarget {
    object Main : CategoriesNavTarget() {
        override val route: String = "main"
    }

    object Category : CategoriesNavTarget() {
        override val base: String = "category"
        override val item: NavItem = CategoryItem
        override val isOptional: Boolean = true
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
        route = CategoriesNavTarget.Category.route(),
        arguments = listOf(navArgument(CategoriesNavTarget.Category.item.value) {
            defaultValue = ""
        }),
        content = {
            val item = nav.getStringElement(CategoryItem) ?: ""
            val viewModel: CategoryEditViewModel = hiltViewModel()

            viewModel.setCategory(item)

            CategoryEditScreen(nav, viewModel)
        }
    )
}

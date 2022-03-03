package com.san.kir.manger.ui.application_navigation.categories

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.categories.category.CategoryEditScreen
import com.san.kir.manger.ui.application_navigation.categories.category.CategoryEditViewModel
import com.san.kir.manger.ui.application_navigation.categories.main.CategoriesScreen
import com.san.kir.manger.utils.compose.NavTarget
import com.san.kir.manger.utils.compose.navTarget
import com.san.kir.manger.utils.compose.navigation

enum class CategoriesNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = "main") {
            CategoriesScreen(
                navigateUp = ::navigateUp,
                navigateToItem = { navigate(Category, it) },
            )
        }
    },

    Category {
        override val content = navTarget(route = "category_item", hasItem = true) {
            val viewModel: CategoryEditViewModel = hiltViewModel()

            viewModel.setCategory(stringElement ?: "")

            CategoryEditScreen(
                navigateUp = ::navigateUp,
                viewModel
            )
        }
    }
}

private val targets = CategoriesNavTarget.values().toList()

fun NavGraphBuilder.categoriesNavGraph(nav: NavHostController) {
    navigation(
        nav = nav,
        startDestination = CategoriesNavTarget.Main,
        route = MainNavTarget.Categories,
        targets = targets
    )
}

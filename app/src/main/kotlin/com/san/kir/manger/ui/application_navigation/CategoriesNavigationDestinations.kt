package com.san.kir.manger.ui.application_navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.categories.ui.categories.CategoriesScreen
import com.san.kir.categories.ui.category.CategoryScreen
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
        override val content = navTarget(route = "category_item", hasItems = true) {
            CategoryScreen(
                navigateUp = ::navigateUp,
                categoryName = stringElement ?: "",
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

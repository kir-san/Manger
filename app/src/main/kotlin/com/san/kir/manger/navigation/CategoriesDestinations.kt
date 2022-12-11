package com.san.kir.manger.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.san.kir.categories.ui.categories.CategoriesScreen
import com.san.kir.categories.ui.category.CategoryScreen
import com.san.kir.manger.navigation.utils.NavTarget
import com.san.kir.manger.navigation.utils.navTarget
import com.san.kir.manger.navigation.utils.navigation

enum class CategoriesNavTarget : NavTarget {
    Main {
        override val content = navTarget(route = GraphTree.Categories.main) {
            CategoriesScreen(
                navigateUp = navigateUp(),
                navigateToItem = rememberNavigateString(Category),
            )
        }
    },

    Category {
        override val content = navTarget(
            route = GraphTree.Categories.item,
            hasItems = true
        ) {
            CategoryScreen(
                navigateUp = navigateUp(),
                categoryName = stringElement() ?: "",
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

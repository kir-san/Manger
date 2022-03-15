package com.san.kir.manger.ui.application_navigation.library.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.compose_utils.TopBarActions
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.AppUpdateService

@Composable
fun libraryActions(
    navigateToOnline: () -> Unit,
    viewModel: LibraryViewModel,
): @Composable TopBarActions.() -> Unit = {
    val categories by viewModel.preparedCategories.collectAsState(emptyList())
    val currentCategoryWithMangas by viewModel.currentCategoryWithManga.collectAsState()
    val context = LocalContext.current

    MenuIcon(icon = Icons.Default.Add, onClick = navigateToOnline)

    ExpandedMenu {
        MenuText(id = R.string.library_menu_reload) {
            currentCategoryWithMangas.mangas.forEach {
                MangaUpdaterService.add(context, it)
            }
        }

        MenuText(id = R.string.library_menu_reload_all) {
            categories.flatMap { it.mangas }.forEach {
                MangaUpdaterService.add(context, it)
            }
        }

        MenuText(id = R.string.library_menu_update) {
            AppUpdateService.start(context)
        }
    }
}

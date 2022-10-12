package com.san.kir.library.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.san.kir.background.services.AppUpdateService
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.compose.TopBarActions
import com.san.kir.library.R
import com.san.kir.library.ui.library.ItemsState
import com.san.kir.library.ui.library.LibraryState

internal fun libraryActions(
    navigateToOnline: () -> Unit,
    state: LibraryState,
): @Composable TopBarActions.() -> Unit = {
    val context = LocalContext.current

    MenuIcon(icon = Icons.Default.Add, onClick = navigateToOnline)

    ExpandedMenu {
        MenuText(id = R.string.library_menu_reload) {
            state.currentCategory.mangas.forEach {
                MangaUpdaterService.add(context, it)
            }
        }

        MenuText(id = R.string.library_menu_reload_all) {
            if (state.items is ItemsState.Ok)
                state.items.items.flatMap { it.mangas }.forEach {
                    MangaUpdaterService.add(context, it)
                }
        }

        MenuText(id = R.string.library_menu_update) {
            AppUpdateService.start(context)
        }
    }
}

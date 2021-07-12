package com.san.kir.manger.ui.drawer.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.san.kir.ankofork.startService
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.ui.AddMangaOnline
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.MenuText

@ExperimentalAnimationApi
@Composable
fun LibraryActions(mainNav: NavHostController) {
    val viewModel: LibraryViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    MenuIcon(icon = Icons.Default.Add) {
        mainNav.navigate(AddMangaOnline.route)
    }

    MenuIcon(icon = Icons.Default.MoreVert) {
        expanded = true
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

        MenuText(id = R.string.library_menu_reload) {
            expanded = false
            currentCategoryWithMangas.mangas.forEach {
                context.startService<MangaUpdaterService>(MangaColumn.tableName to it)
            }
        }

        MenuText(id = R.string.library_menu_reload_all) {
            expanded = false
            state.categories.flatMap { it.mangas }.forEach {
                context.startService<MangaUpdaterService>(MangaColumn.tableName to it)
            }
        }

        MenuText(id = R.string.library_menu_update) {
            expanded = false
        }
    }

}
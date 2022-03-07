package com.san.kir.chapters

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.compose_utils.CheckedMenuText
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.MenuText
import com.san.kir.data.models.base.Manga

@Composable
fun DefaultTopBar(
    navigateUp: () -> Unit,
    viewModel: MainViewModel,
    changeActionState: (Boolean) -> Unit,
) {
    val manga by viewModel.manga.collectAsState()

    TopAppBar(
        title = {
            Text(manga.name, maxLines = 1)
        },

        navigationIcon = {
            IconButton(navigateUp) {
                Icon(Icons.Default.ArrowBack, "")
            }
        },

        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(Dimensions.zero),

        actions = {
            if (manga.name.isNotBlank()) {
                DefaultModeActions(
                    mangaName = manga.name,
                    viewModel = hiltViewModel(),
                    changeActionState = changeActionState,
                )
            }
        },

        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false, applyTop = false
        )
    )
}

@Composable
private fun DefaultModeActions(
    mangaName: String,
    viewModel: DefaultActionViewModel,
    changeActionState: (Boolean) -> Unit,
    context: Context = LocalContext.current,
) {
    viewModel.setMangaUnic(mangaName)

    val manga by viewModel.manga.collectAsState()

    val state = remember(manga) { ActionState(manga) }

    if (state.hasUpdate)
        MenuIcon(icon = Icons.Default.Update) {
            changeActionState(true)
            MangaUpdaterService.add(context, manga)
        }

    MenuIcon(
        icon = Icons.Default.MoreVert,
        onClick = state::expandMenu,
    )

    DropdownMenu(
        expanded = state.showExpandMenu,
        onDismissRequest = state::collapseMenu,
    ) {
        // Быстрая загрузка глав
        MenuText(id = R.string.list_chapters_download_next) {
            state.collapseMenu()
            viewModel.downloadNextNotReadChapter()
        }
        MenuText(id = R.string.list_chapters_download_not_read) {
            state.collapseMenu()
            viewModel.downloadAllNotReadChapters()
        }
        MenuText(id = R.string.list_chapters_download_all) {
            state.collapseMenu()
            viewModel.downloadAllChapters()
        }

        // настройки обновления и сортировки индивидуальные для каждой манги
        CheckedMenuText(
            id = R.string.list_chapters_is_update,
            checked = state.hasUpdate,
        ) {
            state.collapseMenu()
            viewModel.updateManga { it.apply { isUpdate = isUpdate.not() } }
        }
        CheckedMenuText(
            id = R.string.list_chapters_change_sort,
            checked = state.hasAlternativeSort
        ) {
            state.collapseMenu()
            viewModel.updateManga { it.apply { isAlternativeSort = isAlternativeSort.not() } }
        }
    }
}

private class ActionState(manga: Manga) {
    var showExpandMenu by mutableStateOf(false)
        private set

    val hasUpdate by mutableStateOf(manga.isUpdate)
    val hasAlternativeSort by mutableStateOf(manga.isAlternativeSort)

    fun expandMenu() {
        showExpandMenu = true
    }

    fun collapseMenu() {
        showExpandMenu = false
    }
}

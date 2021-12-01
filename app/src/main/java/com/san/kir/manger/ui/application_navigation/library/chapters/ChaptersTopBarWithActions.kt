package com.san.kir.manger.ui.application_navigation.library.chapters

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
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.TopAppBar
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.MangaUpdaterService
import com.san.kir.manger.utils.compose.CheckedMenuText
import com.san.kir.manger.utils.compose.MenuIcon
import com.san.kir.manger.utils.compose.MenuText

@Composable
fun ChaptersTopBar(
    navigateUp: () -> Unit,
    viewModel: ChaptersViewModel,
    changeAction: (Boolean) -> Unit,
) {
    TopAppBar(
        title = {
            Text(viewModel.manga.name, maxLines = 1)
        },
        navigationIcon = {
            IconButton(navigateUp) {
                Icon(Icons.Default.ArrowBack, "")
            }
        },
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(0.dp),
        actions = {
            if (viewModel.manga.name.isNotBlank())
                Actions(chaptersActionViewModel(viewModel.manga.name), changeAction)
        },
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false, applyTop = false
        )
    )
}

@Composable
private fun Actions(
    viewModel: ChaptersActionViewModel,
    changeAction: (Boolean) -> Unit,
    context: Context = LocalContext.current,
) {
    val manga by viewModel.manga.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val visibleUpdate by remember(manga) { mutableStateOf(manga.isUpdate) }
    val alternativeSort by remember(manga) { mutableStateOf(manga.isAlternativeSort) }

    if (visibleUpdate)
        MenuIcon(icon = Icons.Default.Update) {
            changeAction(true)
            MangaUpdaterService.add(context, manga)
        }

    MenuIcon(icon = Icons.Default.MoreVert) {
        expanded = true
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        // Быстрая загрузка глав
        MenuText(id = R.string.list_chapters_download_next) {
            expanded = false
            viewModel.downloadNextNotReadChapter()
        }
        MenuText(id = R.string.list_chapters_download_not_read) {
            expanded = false
            viewModel.downloadAllNotReadChapters()
        }
        MenuText(id = R.string.list_chapters_download_all) {
            expanded = false
            viewModel.downloadAllChapters()
        }

        // настройки обновления и сортировки индивидуальные для каждой манги
        CheckedMenuText(
            id = R.string.list_chapters_is_update,
            checked = visibleUpdate,
        ) {
            expanded = false
            viewModel.updateManga { it.apply { isUpdate = isUpdate.not() } }
        }
        CheckedMenuText(
            id = R.string.list_chapters_change_sort,
            checked = alternativeSort
        ) {
            expanded = false
            viewModel.updateManga { it.apply { isAlternativeSort = isAlternativeSort.not() } }
        }
    }
}


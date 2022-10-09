package com.san.kir.chapters

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.compose_utils.TopBarActions
import com.san.kir.core.compose_utils.topBar
import com.san.kir.data.models.base.Manga

@Composable
fun defaultTopBar(
    navigateUp: () -> Unit,
    manga: Manga,
    changeActionState: (Boolean) -> Unit,
) = topBar(
    title = manga.name,
    navigationListener = navigateUp,
    actions = defaultModeActions(
        manga.name,
        viewModel = hiltViewModel(),
        changeActionState = changeActionState,
    )
)

private fun defaultModeActions(
    mangaName: String,
    viewModel: DefaultActionViewModel,
    changeActionState: (Boolean) -> Unit,
): @Composable TopBarActions.() -> Unit = {
    if (mangaName.isNotBlank()) {
        viewModel.setMangaUnic(mangaName)

        val context = LocalContext.current

        val manga by viewModel.manga.collectAsState()

        if (manga.isUpdate)
            MenuIcon(icon = Icons.Default.Update) {
                changeActionState(true)
                MangaUpdaterService.add(context, manga)
            }

        ExpandedMenu {
            // Быстрая загрузка глав
            MenuText(id = R.string.list_chapters_download_next) {
                viewModel.downloadNextNotReadChapter()
            }
            MenuText(id = R.string.list_chapters_download_not_read) {
                viewModel.downloadAllNotReadChapters()
            }
            MenuText(id = R.string.list_chapters_download_all) {
                viewModel.downloadAllChapters()
            }

            // настройки обновления и сортировки индивидуальные для каждой манги
            CheckedMenuText(
                id = R.string.list_chapters_is_update,
                checked = manga.isUpdate,
            ) {
                viewModel.updateManga { it.copy(isUpdate = it.isUpdate.not()) }
            }
            CheckedMenuText(
                id = R.string.list_chapters_change_sort,
                checked = manga.isAlternativeSort
            ) {
                viewModel.updateManga { it.copy(isAlternativeSort = it.isAlternativeSort.not()) }
            }
        }
    }
}

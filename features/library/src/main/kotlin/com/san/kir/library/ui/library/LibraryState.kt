package com.san.kir.library.ui.library

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.san.kir.core.support.MainMenuType
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.CategoryWithMangas
import com.san.kir.data.models.extend.SimplifiedManga
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toImmutableList

@Immutable
internal data class LibraryState(
    val selectedManga: SelectedMangaState,
    val currentCategory: CategoryWithMangas,
    val items: ItemsState,
    val showCategory: Boolean,
    val background: BackgroundState,
) : ScreenState

internal sealed interface ItemsState {
    object Empty : ItemsState
    object Load : ItemsState

    @Stable
    data class Ok(
        val items: ImmutableList<CategoryWithMangas>,
        //    Имена всех категорий и их id
        val categories: PersistentMap<Long, String>,
        // Имена категорий с количество содержимой манги для заголовка вкладок
        val names: ImmutableList<String> =
            items
                .map { cat -> "${cat.name}: ${cat.mangas.count()}" }
                .toImmutableList()
    ) : ItemsState
}


internal sealed interface SelectedMangaState {
    object NonVisible : SelectedMangaState
    data class Visible(val item: SimplifiedManga) : SelectedMangaState
}

internal sealed interface BackgroundState {
    object Work : BackgroundState
    object None : BackgroundState
}

//@Immutable
data class LibraryNavigation(
    val navigateToScreen: (MainMenuType) -> Unit,
    val navigateToCategories: () -> Unit,
    val navigateToCatalogs: () -> Unit,
    val navigateToInfo: (String) -> Unit,
    val navigateToStorage: (Long) -> Unit,
    val navigateToStats: (Long) -> Unit,
    val navigateToChapters: (String) -> Unit,
    val navigateToOnline: () -> Unit,
)

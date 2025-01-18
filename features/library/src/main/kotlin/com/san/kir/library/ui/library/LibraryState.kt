package com.san.kir.library.ui.library

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.main.CategoryWithMangas
import com.san.kir.data.models.utils.MainMenuType

@Immutable
internal data class LibraryState(
    val currentCategory: CategoryWithMangas = CategoryWithMangas(),
    val items: ItemsState = ItemsState.Load,
    val showCategory: Boolean = false,
    val background: BackgroundState = BackgroundState.None,
) : ScreenState {
    val hasTabs: Boolean = items is ItemsState.Ok && items.names.size > 1
    val singleTab: String? = if (items is ItemsState.Ok && items.names.size == 1) items.names.first() else null
}

@Stable
internal sealed interface ItemsState {
    @Immutable
    data object Empty : ItemsState
    @Immutable
    data object Load : ItemsState

    @Immutable
    data class Ok(
        val items: List<CategoryWithMangas>,
        //    Имена всех категорий и их id
        val categories: Map<Long, String> = emptyMap(),
        // Имена категорий с количество содержимой манги для заголовка вкладок
        val names: List<String> = items.map { cat -> "${cat.name}: ${cat.mangas.count()}" }
    ) : ItemsState
}

internal enum class BackgroundState { Work, None }

@Immutable
internal data class LibraryNavigation(
    val toScreen: (MainMenuType) -> Unit,
    val toInfo: (Long, SharedParams) -> Unit,
    val toStorage: (Long, SharedParams) -> Unit,
    val toStats: (Long, SharedParams) -> Unit,
    val toChapters: (Long, SharedParams) -> Unit,
    val toOnline: (SharedParams) -> Unit,
)

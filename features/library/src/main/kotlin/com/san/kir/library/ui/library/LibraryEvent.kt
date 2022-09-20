package com.san.kir.library.ui.library

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.extend.CategoryWithMangas
import com.san.kir.data.models.extend.SimplifiedManga

internal sealed interface LibraryEvent : ScreenEvent {
    data class SelectManga(val item: SimplifiedManga) : LibraryEvent
    object NonSelect : LibraryEvent
    data class SetCurrentCategory(val item: CategoryWithMangas) : LibraryEvent
    data class ChangeCategory(val categoryId: Long) : LibraryEvent
    data class DeleteManga(val mangaId: Long, val withFiles: Boolean) : LibraryEvent
}

package com.san.kir.data.models.extend

import androidx.compose.runtime.Immutable
import com.san.kir.core.support.SortLibraryUtil

@Immutable
data class CategoryWithMangas(
    val id: Long = 0,
    val name: String = "",
    val typeSort: String = SortLibraryUtil.abc,
    val isReverseSort: Boolean = false,
    val spanPortrait: Int = 2,
    val spanLandscape: Int = 3,
    val isLargePortrait: Boolean = true,
    val isLargeLandscape: Boolean = true,
    val mangas: List<SimplifiedManga> = emptyList(),
)

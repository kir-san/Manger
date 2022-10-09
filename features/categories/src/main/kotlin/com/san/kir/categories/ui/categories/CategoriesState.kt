package com.san.kir.categories.ui.categories

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Category
import kotlinx.collections.immutable.ImmutableList

@Stable
internal data class CategoriesState(
    val items: ImmutableList<Category>
) : ScreenState

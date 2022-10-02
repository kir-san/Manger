package com.san.kir.categories.ui.category

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Category
import kotlinx.collections.immutable.ImmutableList

data class CategoryState(
    val category: Category,
    val categoryNames: ImmutableList<String>,
    val hasCreatedNew: Boolean,
    val oldCategoryName: String,
    val hasAll: Boolean,
    val hasChanges: Boolean,
) : ScreenState

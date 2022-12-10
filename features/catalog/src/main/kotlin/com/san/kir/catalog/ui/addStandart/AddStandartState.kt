package com.san.kir.catalog.ui.addStandart

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class AddStandartState(
    val categoryName: String = "",
    val createNewCategory: Boolean = false,
    val hasAllow: Boolean = false,
    val availableCategories: ImmutableList<String> = persistentListOf(),
    val progress: Int = 0,
    val processState: ProcessState = ProcessState.None,
) : ScreenState

internal sealed interface ProcessState {
    data object Load : ProcessState
    data object Error : ProcessState
    data object Complete : ProcessState
    data object None : ProcessState
}

internal object ProcessStatus {
    const val categoryChanged = 1
    const val prevAndUpdateManga = 2
    const val prevAndCreatedFolder = 3
    const val prevAndSearchChapters = 4
    const val allComplete = 5
}

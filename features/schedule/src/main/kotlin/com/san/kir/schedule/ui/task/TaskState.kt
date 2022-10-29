package com.san.kir.schedule.ui.task

import androidx.compose.runtime.Stable
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.PlannedTask
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
internal data class TaskState(
    val item: PlannedTask,
    val categoryName: String,
    val mangaName: String,
    val groupNames: ImmutableList<String>,
    val categoryIds: ImmutableList<Long>,
    val categoryNames: ImmutableList<String>,
    val catalogNames: ImmutableList<String>,
    val mangaIds: ImmutableList<Long>,
    val mangaNames: ImmutableList<String>,
    val hasChanges: Boolean
) : ScreenState {
    companion object {
        val weeks = persistentListOf(*PlannedWeek.values())
        val periods = persistentListOf(*PlannedPeriod.values())
        val types = persistentListOf(*PlannedType.values())
    }
}


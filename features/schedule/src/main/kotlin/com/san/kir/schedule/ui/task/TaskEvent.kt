package com.san.kir.schedule.ui.task

import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface TaskEvent : ScreenEvent {
    data class Set(val itemId: Long) : TaskEvent
    data class Change(val type: ChangeType) : TaskEvent
    object Save : TaskEvent
}

internal sealed interface ChangeType {
    data class Type(val type: PlannedType) : ChangeType
    data class Manga(val mangaId: Long) : ChangeType
    data class Group(val name: String) : ChangeType
    data class Mangas(val mangaIds: List<Long>) : ChangeType
    data class Category(val categoryId: Long) : ChangeType
    data class Catalog(val name: String) : ChangeType
    data class Period(val period: PlannedPeriod) : ChangeType
    data class Day(val day: PlannedWeek) : ChangeType
    data class Time(val hour: Int, val minute: Int) : ChangeType
}

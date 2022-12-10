package com.san.kir.schedule.ui.task

import android.app.Application
import com.san.kir.background.works.ScheduleWorker
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.base.PlannedTaskBase
import com.san.kir.schedule.logic.repo.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class TaskViewModel @Inject constructor(
    private val context: Application,
    private val tasksRepository: TasksRepository,
) : BaseViewModel<TaskEvent, TaskState>() {
    private val item = MutableStateFlow(PlannedTask())
    private val hasChanges = MutableStateFlow(false)
    private var isNew = false

    override val tempState = combine(
        item,
        tasksRepository.categories,
        tasksRepository.mangas,
        hasChanges
    ) { item, categories, mangas, changes ->

        val newItem = item.copy(
            mangas = item.mangas.ifEmpty { item.groupContent.mapNotNull { name -> mangas.firstOrNull { it.name == name }?.id } }
        )

        TaskState(
            item = newItem,
            categoryName = categories.firstOrNull { it.id == newItem.categoryId }?.name ?: "",
            mangaName = mangas.firstOrNull { it.id == newItem.mangaId }?.name ?: "",
            categoryIds = categories.map { it.id }.toImmutableList(),
            categoryNames = categories.map { it.name }.toImmutableList(),
            mangaIds = mangas.map { it.id }.toImmutableList(),
            mangaNames = mangas.map { it.name }.toImmutableList(),
            catalogNames = tasksRepository.catalogs.toImmutableList(),
            groupNames = newItem.mangas.map { id -> mangas.first { it.id == id }.name }
                .toImmutableList(),
            hasChanges = changes,
        )
    }

    override val defaultState = TaskState(
        item = PlannedTask(),
        categoryName = "",
        mangaName = "",
        categoryIds = persistentListOf(),
        categoryNames = persistentListOf(),
        catalogNames = persistentListOf(),
        mangaIds = persistentListOf(),
        mangaNames = persistentListOf(),
        groupNames = persistentListOf(),
        hasChanges = false,
    )

    override suspend fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.Set -> set(event.itemId)
            is TaskEvent.Change -> change(event.type)
            TaskEvent.Save -> save()
        }
    }

    private suspend fun set(taskId: Long) {
        item.update { tasksRepository.item(taskId) ?: it }
        isNew = taskId == -1L
    }

    private suspend fun save() {
        if (isNew) {
            tasksRepository.save(item.value.copy(addedTime = System.currentTimeMillis()), true)
        } else {
            tasksRepository.save(item.value.copy(isEnabled = false), false)
            ScheduleWorker.cancelTask(context, object : PlannedTaskBase {
                override val id: Long = item.value.id
                override val manga: String = state.value.mangaName
                override val groupName: String = item.value.groupName
                override val category: String = state.value.categoryName
                override val catalog: String = item.value.catalog
                override val type: PlannedType = item.value.type
                override val period: PlannedPeriod = item.value.period
                override val dayOfWeek: PlannedWeek = item.value.dayOfWeek
                override val hour: Int = item.value.hour
                override val minute: Int = item.value.minute
            })
        }
        hasChanges.value = false
    }

    private fun change(type: ChangeType) {
        item.update {
            when (type) {
                is ChangeType.Catalog -> it.copy(catalog = type.name)
                is ChangeType.Category -> it.copy(categoryId = type.categoryId)
                is ChangeType.Day -> it.copy(dayOfWeek = type.day)
                is ChangeType.Group -> it.copy(groupName = type.name)
                is ChangeType.Manga -> it.copy(mangaId = type.mangaId)
                is ChangeType.Mangas -> it.copy(mangas = type.mangaIds)
                is ChangeType.Period -> it.copy(period = type.period)
                is ChangeType.Time -> it.copy(hour = type.hour, minute = type.minute)
                is ChangeType.Type -> it.copy(type = type.type)
            }
        }
        hasChanges.value = true
    }
}

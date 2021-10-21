package com.san.kir.manger.ui.application_navigation.schedule

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.manger.R
import com.san.kir.manger.components.schedule.ScheduleManager
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.di.MainDispatcher
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlannedTaskViewModel @AssistedInject constructor(
    @Assisted private val taskId: Long,
    private val context: Application,
    private val plannedDao: PlannedDao,
    mangaDao: MangaDao,
    categoryDao: CategoryDao,
    siteDao: SiteDao,
    private val manager: ScheduleManager,
    @DefaultDispatcher private val default: CoroutineDispatcher,
    @MainDispatcher private val main: CoroutineDispatcher,
) : ViewModel() {
    var task by mutableStateOf(PlannedTask())

    val title by derivedStateOf {
        if (taskId == -1L)
            context.getString(R.string.planned_task_title_create)
        else
            context.getString(R.string.planned_task_title_change)
    }

    var listManga by mutableStateOf(listOf<Manga>())
        private set
    val listMangaName by derivedStateOf { listManga.map { it.name } }
    val listMangaUnic by derivedStateOf { listManga.map { it.unic } }

    var categoryList by mutableStateOf(listOf<String>())
        private set
    var catalogList by mutableStateOf(listOf<String>())
        private set

    init {
        viewModelScope.launch(default) {
            if (taskId != -1L) {
                plannedDao.loadItem(taskId)
                    .filterNotNull()
                    .first()
                    .let { withContext(main) { task = it } }
            }

            mangaDao.loadItems()
                .first()
                .filter { it.isUpdate }
                .let { withContext(main) { listManga = it } }

            categoryDao.loadItems()
                .first()
                .map { it.name }
                .let { withContext(main) { categoryList = it} }

            siteDao.loadItems()
                .first()
                .map { it.name }
                .let { withContext(main) { catalogList = it} }
        }
    }

    fun save() = viewModelScope.launch(default) {
        if (taskId != -1L) {
            plannedDao.update(task.copy(isEnabled = false))
            manager.cancel(context, task)
        } else {
            plannedDao.insert(task.copy(addedTime = System.currentTimeMillis()))
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: Long): PlannedTaskViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            taskId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(taskId) as T
            }
        }
    }
}

@Composable
fun plannedTaskViewModel(taskId: Long): PlannedTaskViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).plannedTaskViewModelFactory()

    return viewModel(factory = PlannedTaskViewModel.provideFactory(factory, taskId))
}

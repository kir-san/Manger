package com.san.kir.manger.ui.application_navigation.schedule

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.mainLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.workmanager.ScheduleWorker
import com.san.kir.manger.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class PlannedTaskViewModel @AssistedInject constructor(
    @Assisted private val taskId: Long,
    private val context: Application,
    private val plannedDao: PlannedDao,
    mangaDao: MangaDao,
    private val categoryDao: CategoryDao,
    siteDao: SiteDao,
) : ViewModel() {
    var task by mutableStateOf(PlannedTask())

    val title by derivedStateOf {
        if (taskId == -1L)
            context.getString(R.string.planned_task_title_create)
        else
            context.getString(R.string.planned_task_title_change)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryName = snapshotFlow { task }
        .flatMapLatest { categoryDao.loadItemById(it.categoryId) }
        .filterNotNull()
        .map { it.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private var listManga by mutableStateOf(listOf<Manga>())
    val listMangaName by derivedStateOf { listManga.map { it.name } }

    var catalogList by mutableStateOf(listOf<String>())
        private set

    val categoryNameList = categoryDao.loadItems().map { list -> list.map { c -> c.name } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categoryIdList = categoryDao.loadItems().map { list -> list.map { c -> c.id } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.defaultLaunch {
            if (taskId != -1L) {
                plannedDao.loadItem(taskId)
                    .filterNotNull()
                    .first()
                    .let { withMainContext { task = it } }
            }

            mangaDao.loadItems()
                .first()
                .filter { it.isUpdate }
                .let { withMainContext { listManga = it } }

            siteDao.loadItems()
                .first()
                .map { it.name }
                .let { withMainContext { catalogList = it } }
        }
    }

    fun save(onSave: () -> Unit) {
        viewModelScope.defaultLaunch {
            if (taskId != -1L) {
                plannedDao.update(task.copy(isEnabled = false))
                ScheduleWorker.cancelTask(context, task)
            } else {
                plannedDao.insert(task.copy(addedTime = System.currentTimeMillis()))
            }
        }.invokeOnCompletion { viewModelScope.mainLaunch { onSave() } }
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
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
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

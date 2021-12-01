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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.manger.R
import com.san.kir.manger.data.room.dao.CategoryDao
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.dao.PlannedDao
import com.san.kir.manger.data.room.dao.SiteDao
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.manger.data.room.entities.PlannedTask
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.utils.coroutines.mainLaunchInVM
import com.san.kir.manger.utils.coroutines.withMainContext
import com.san.kir.manger.foreground_work.workmanager.ScheduleWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first


class PlannedTaskViewModel @AssistedInject constructor(
    @Assisted private val taskId: Long,
    private val context: Application,
    private val plannedDao: PlannedDao,
    mangaDao: MangaDao,
    categoryDao: CategoryDao,
    siteDao: SiteDao,
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
    val listMangaUnic by derivedStateOf { listManga.map { it.name } }

    var categoryList by mutableStateOf(listOf<String>())
        private set
    var catalogList by mutableStateOf(listOf<String>())
        private set

    init {
        defaultLaunchInVM {
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

            categoryDao.loadItems()
                .first()
                .map { it.name }
                .let { withMainContext { categoryList = it } }

            siteDao.loadItems()
                .first()
                .map { it.name }
                .let { withMainContext { catalogList = it } }
        }
    }

    fun save(onSave: () -> Unit) {
        defaultLaunchInVM {
            if (taskId != -1L) {
                plannedDao.update(task.copy(isEnabled = false))
                ScheduleWorker.cancelTask(context, task)
            } else {
                plannedDao.insert(task.copy(addedTime = System.currentTimeMillis()))
            }
        }.invokeOnCompletion { mainLaunchInVM { onSave() } }
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

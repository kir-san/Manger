package com.san.kir.manger.ui.application_navigation.startapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Operation
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.log
import com.san.kir.data.store.FirstLaunchStore
import com.san.kir.manger.foreground_work.workmanager.FirstInitAppWorker
import com.san.kir.manger.foreground_work.workmanager.MigrateLatestChapterToChapterWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@HiltViewModel
class StartAppViewModel @Inject constructor(
    private val dataStore: FirstLaunchStore,
    private val ctx: Application,
) : ViewModel() {
    private val _initState = MutableStateFlow(OperationState.IN_PROGRESS)
    val initState = _initState.asStateFlow()

    @OptIn(ExperimentalTime::class)
    fun startApp() {
        viewModelScope.launch {
            createNeedFolders()
            dataStore.data.collect { data ->
                delay(Duration.seconds(0.5))
                if (data.isFirstLaunch.not()) {
                    dataStore.initFirstLaunch()
                    initApp()
                } else {
                    _initState.update { OperationState.SUCCESS }
                }
            }

        }
    }

    private suspend fun initApp() {
        FirstInitAppWorker.addTask(ctx).state.asFlow().collect {
            log(it.toString())
            when (it) {
                is Operation.State.SUCCESS -> {
                    _initState.update { OperationState.SUCCESS }
                }
                is Operation.State.FAILURE -> {
                    _initState.update { OperationState.FAILURE }
                }
                else -> {
                    _initState.update { OperationState.IN_PROGRESS }
                }
            }

        }
        MigrateLatestChapterToChapterWorker.addTask(ctx)
    }

    private fun createNeedFolders() {
        com.san.kir.core.support.DIR.ALL.forEach { dir -> getFullPath(dir).createDirs() }
    }
}

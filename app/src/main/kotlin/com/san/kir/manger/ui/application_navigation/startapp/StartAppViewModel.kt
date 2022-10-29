package com.san.kir.manger.ui.application_navigation.startapp

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Operation
import com.san.kir.background.services.CatalogForOneSiteUpdaterService
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.getFullPath
import com.san.kir.manger.foreground_work.workmanager.FirstInitAppWorker
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.utils.compose.deepLinkIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class StartAppViewModel @Inject constructor(
    private val ctx: Application,
) : ViewModel() {
    private val _initState = MutableStateFlow(OperationState.IN_PROGRESS)
    val initState = _initState.asStateFlow()

    fun startApp() {
        viewModelScope.launch {
            createNeedFolders()
            val sp = ctx.getSharedPreferences("startup", Context.MODE_PRIVATE)

            MangaUpdaterService.setLatestDeepLink(
                ctx, ctx.deepLinkIntent<MainActivity>(MainNavTarget.Latest),
            )

            CatalogForOneSiteUpdaterService.setLatestDeepLink(
                ctx, ctx.deepLinkIntent<MainActivity>(CatalogsNavTarget.Main)
            )

            delay(0.5.seconds)
            if (sp.contains("firstLaunch")) {
                _initState.update { OperationState.SUCCESS }
            } else {
                sp.edit {
                    putBoolean("startup", false)
                }
                initApp()
            }
        }
    }

    private suspend fun initApp() {
        FirstInitAppWorker.addTask(ctx).state.asFlow().collectLatest {
            Timber.v(it.toString())
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
    }

    private fun createNeedFolders() {
        DIR.ALL.forEach { dir -> getFullPath(dir).createDirs() }
    }
}

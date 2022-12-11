package com.san.kir.manger.ui.init

import android.app.Application
import androidx.lifecycle.ViewModel
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.background.works.UpdateCatalogWorker
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.getFullPath
import com.san.kir.manger.navigation.CatalogsNavTarget
import com.san.kir.manger.navigation.MainNavTarget
import com.san.kir.manger.navigation.utils.deepLinkIntent
import com.san.kir.manger.ui.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class InitViewModel @Inject constructor(
    private val ctx: Application,
    private val repository: InitRepository,
) : ViewModel() {

    suspend fun startApp() {
        createNeedFolders()

        MangaUpdaterService.setLatestDeepLink(
            ctx, ctx.deepLinkIntent<MainActivity>(MainNavTarget.Latest),
        )

        UpdateCatalogWorker.setLatestDeepLink(
            ctx, ctx.deepLinkIntent<MainActivity>(CatalogsNavTarget.Main)
        )

        delay(0.5.seconds)

        if (repository.isFirstLaunch()) repository.restoreSchedule()
    }

    private suspend fun createNeedFolders() = withIoContext {
        DIR.ALL.forEach { dir -> getFullPath(dir).createDirs() }
    }
}

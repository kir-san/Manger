package com.san.kir.manger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.internet.LocalConnectManager
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.SiteCatalogItemViewModel
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogViewModel
import com.san.kir.manger.ui.application_navigation.schedule.PlannedTaskViewModel
import com.san.kir.manger.ui.application_navigation.startapp.StartAppScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun catalogViewModelFactory(): CatalogViewModel.Factory
        fun mangaStorageViewModelFactory(): MangaStorageViewModel.Factory
        fun onlyMangaViewModelFactory(): OnlyMangaViewModel.Factory
        fun siteCatalogItemViewModelFactory(): SiteCatalogItemViewModel.Factory
        fun plannedTaskViewModelFactory(): PlannedTaskViewModel.Factory
    }

    @Inject
    lateinit var connectManager: ConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var isSplash by rememberSaveable { mutableStateOf(true) }

            if (isSplash)
                StartAppScreen {
                    Timber.w("Go to library")
                    isSplash = false
                }
            else
                CompositionLocalProvider(LocalConnectManager provides connectManager) {
                    MangerApp()
                }
        }
    }
}



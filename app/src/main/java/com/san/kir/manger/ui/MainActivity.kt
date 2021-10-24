package com.san.kir.manger.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.MangaStorageViewModel
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.SiteCatalogItemViewModel
import com.san.kir.manger.ui.application_navigation.catalog.catalog.CatalogViewModel
import com.san.kir.manger.ui.application_navigation.categories.OnlyCategoryViewModel
import com.san.kir.manger.ui.application_navigation.library.chapters.ChaptersActionViewModel
import com.san.kir.manger.ui.application_navigation.library.chapters.ChaptersViewModel
import com.san.kir.manger.ui.application_navigation.schedule.PlannedTaskViewModel
import com.san.kir.manger.ui.application_navigation.statistic.OnlyStatisticViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun chaptersActionViewModelFactory(): ChaptersActionViewModel.Factory
        fun chaptersViewModelFactory(): ChaptersViewModel.Factory
        fun catalogViewModelFactory(): CatalogViewModel.Factory
        fun mangaStorageViewModelFactory(): MangaStorageViewModel.Factory
        fun onlyMangaViewModelFactory(): OnlyMangaViewModel.Factory
        fun onlyCategoryViewModelFactory(): OnlyCategoryViewModel.Factory
        fun siteCatalogItemViewModelFactory(): SiteCatalogItemViewModel.Factory
        fun onlyStatisticViewModelFactory(): OnlyStatisticViewModel.Factory
        fun plannedTaskViewModelFactory(): PlannedTaskViewModel.Factory
    }

    private val mainViewModel: MainViewModel by viewModels()

    private val catalogReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            mainViewModel.catalogReceiver(intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IntentFilter(CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE).apply {
            registerReceiver(catalogReceiver, this)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CompositionLocalProvider(LocalBaseViewModel provides mainViewModel) {
                val darkTheme by mainViewModel.darkTheme.collectAsState()
                MaterialTheme(colors = if (darkTheme) darkColors() else lightColors()) {
                    // Remember a SystemUiController
                    val systemUiController = rememberSystemUiController()
                    val useDarkIcons = MaterialTheme.colors.isLight

                    SideEffect {
                        // Update all of the system bar colors to be transparent, and use
                        // dark icons if we're in light theme
                        systemUiController.setSystemBarsColor(
                            color = Color.Transparent,
                            darkIcons = useDarkIcons
                        )
                    }
                    MangerApp()
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(catalogReceiver)
        super.onDestroy()
    }
}



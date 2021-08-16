package com.san.kir.manger.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.ui.application_navigation.chapters.ChaptersActionViewModel
import com.san.kir.manger.ui.application_navigation.chapters.ChaptersViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent


@ExperimentalPermissionsApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun chaptersActionViewModelFactory(): ChaptersActionViewModel.Factory
        fun chaptersViewModelFactory(): ChaptersViewModel.Factory
    }

    private val mainViewModel: MainViewModel by viewModels()

    private val catalogReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            mainViewModel.catalogReceiver(intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT))
        }
    }

    private val chaptersReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (intent.action == MangaUpdaterService.actionGet) {
                    mainViewModel.chaptersReceiver(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IntentFilter(CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE).apply {
            registerReceiver(catalogReceiver, this)
        }

        IntentFilter().apply {
            addAction(MangaUpdaterService.actionGet)
            registerReceiver(chaptersReceiver, this)
        }

        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            CompositionLocalProvider(LocalBaseViewModel provides mainViewModel) {
                MangerApp(::closeActivity)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(catalogReceiver)
        unregisterReceiver(chaptersReceiver)
        super.onDestroy()
    }

    private fun closeActivity() {
        finishAffinity()
    }
}



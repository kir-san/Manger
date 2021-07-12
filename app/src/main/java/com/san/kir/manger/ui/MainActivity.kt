package com.san.kir.manger.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.san.kir.manger.data.datastore.FirstLaunchRepository
import com.san.kir.manger.data.datastore.firstLaunchStore
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.workmanager.FirstInitAppWorker
import com.san.kir.manger.workmanager.MigrateLatestChapterToChapterWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@ExperimentalPermissionsApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            mainViewModel.catalogReceiver(intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IntentFilter(CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE).apply {
            registerReceiver(receiver, this)
        }

        setContent {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            CompositionLocalProvider(LocalBaseViewModel provides mainViewModel) {
                MangerApp(::closeActivity)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun closeActivity() {
        finishAffinity()
    }
}



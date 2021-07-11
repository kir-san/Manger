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
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.pager.ExperimentalPagerApi
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


@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

   private val mainViewModel: MainViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            mainViewModel.catalogReceiver(intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT))
        }
    }

    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (compatCheckSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED)
//            setContent {
//                AlertDialog(
//                    onDismissRequest = {
//                        longToast(R.string.main_permission_error)
//                        finishAffinity()
//                    },
//                    confirmButton = {
//                        Button(onClick = {/* handlePermissions() */}) {
//                            Text(text = "Ok")
//                        }
//                    },
//                    dismissButton = {
//                        Button(onClick = { finishAffinity() }) {
//                            Text(text = "Уйти")
//                        }
//                    },
//                    title = {
//                        Text(text = "Внимание")
//                    },
//                    text = {
//                        Text(text = "ОЧень важное сообщение")
//                    }
//                )
//            }
//        else
//            init()

    }

//    @ExperimentalComposeUiApi
//    @ExperimentalAnimationApi
//    private fun handlePermissions() {
//        PermissionManager.requestPermissions(
//            this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) {
//            requestCode = 200
//            resultCallback = {
//                when (this) {
//                    is PermissionResult.PermissionGranted -> {
//                        // Добавьте сюда свою логику после того, как пользователь предоставит разрешение (а)
//                        init()
//                    }
//                    is PermissionResult.PermissionDenied -> {
//                        // Добавьте свою логику для обработки отказа в разрешении
//                        longToast(R.string.main_permission_error)
//                        finishAffinity()
//                    }
//                    is PermissionResult.PermissionDeniedPermanently -> {
//                        // Добавьте сюда свою логику, если пользователь навсегда отказал в разрешении (разрешениях).
//                        // В идеале вы должны попросить пользователя вручную перейти к настройкам и включить разрешения
//                    }
//                    is PermissionResult.ShowRational -> {
//                        // Если пользователь часто отказывает в разрешении, он / она не понимает, почему вы запрашиваете это разрешение.
//                        // Это ваш шанс объяснить им, почему вам нужно разрешение..
//                    }
//                }
//            }
//        }
//    }


    @ExperimentalComposeUiApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    private fun init() {
        val dataStore = FirstLaunchRepository(firstLaunchStore)
        lifecycleScope.launch(Dispatchers.Main) {

            dataStore.data.collect { data ->
                if (data.isFirstLaunch.not()) {
                    dataStore.initFirstLaunch()
                    FirstInitAppWorker.addTask(this@MainActivity)
                    val task = OneTimeWorkRequestBuilder<MigrateLatestChapterToChapterWorker>()
                        .addTag("migrate")
                        .build()
                    WorkManager.getInstance(applicationContext).enqueue(task)
                }
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LocalView.current.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            CompositionLocalProvider(LocalBaseViewModel provides mainViewModel) {
                MangerApp(::closeActivity)
            }
        }

        IntentFilter(CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE).apply {
            registerReceiver(receiver, this)
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



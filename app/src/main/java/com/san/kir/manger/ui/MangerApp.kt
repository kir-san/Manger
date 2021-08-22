package com.san.kir.manger.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.san.kir.manger.R
import com.san.kir.manger.data.datastore.FirstLaunchRepository
import com.san.kir.manger.data.datastore.firstLaunchStore
import com.san.kir.manger.ui.application_navigation.applicationGraph
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.workmanager.FirstInitAppWorker
import com.san.kir.manger.workmanager.MigrateLatestChapterToChapterWorker
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect

@OptIn(
    ExperimentalPermissionsApi::class,
    InternalCoroutinesApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MangerApp(close: () -> Unit) {
    RequiresPermission {
        MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
            ProvideWindowInsets {
                val ctx = LocalContext.current
                val dataStore = FirstLaunchRepository(ctx.firstLaunchStore)
                LaunchedEffect(true) {
                    dataStore.data.collect { data ->
                        if (data.isFirstLaunch.not()) {
                            dataStore.initFirstLaunch()
                            FirstInitAppWorker.addTask(ctx)
                            val task =
                                OneTimeWorkRequestBuilder<MigrateLatestChapterToChapterWorker>()
                                    .addTag("migrate")
                                    .build()
                            WorkManager.getInstance(ctx).enqueue(task)
                        }
                    }
                }

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

                val mainNavController = rememberAnimatedNavController()
                AnimatedNavHost(
                    navController = mainNavController,
                    graph = mainNavController.applicationGraph(close)
                )
            }

        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun RequiresPermission(onSuccess: @Composable () -> Unit) {
    // Track if the user doesn't want to see the rationale any more.
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    // Storage permission state
    val storagePermissionState = rememberPermissionState(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    when {
        // permission is granted
        storagePermissionState.hasPermission -> {
            createNeedFolders()
            onSuccess()
        }

        storagePermissionState.shouldShowRationale || !storagePermissionState.permissionRequested -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    if (doNotShowRationale) {
                        Text(stringResource(R.string.main_permission_error))
                    } else {
                        Text(stringResource(R.string.main_permission_reason))
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Button(onClick = { storagePermissionState.launchPermissionRequest() }) {
                                Text(stringResource(R.string.main_permission_request))
                            }
                            Spacer(Modifier.width(16.dp))
                            Button(onClick = { doNotShowRationale = true }) {
                                Text(stringResource(R.string.main_permission_no_rationale))
                            }
                        }
                    }
                }
            }
        }

        else -> {
            Text(stringResource(R.string.main_permission_nonpermission))
        }

    }
}

private fun createNeedFolders() {
    DIR.ALL.forEach { dir -> getFullPath(dir).createDirs() }
}

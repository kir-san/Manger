package com.san.kir.manger.ui.init

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.core.compose.animation.FromTopToTopAnimContent
import com.san.kir.manger.R
import timber.log.Timber

@Composable
fun InitScreen(navigateToItem: () -> Unit) {
    val viewModel: InitViewModel = hiltViewModel()
    var state by remember { mutableStateOf<InitState>(InitState.Memory) }

    MaterialTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.default),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(3f)
                ) {
                    Image(
                        painterResource(R.mipmap.ic_launcher_foreground),
                        "app icon",
                        modifier = Modifier.size(300.dp),
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(220.dp),
                        color = Color.Black,
                        strokeWidth = Dimensions.half
                    )
                }

                FromBottomToBottomAnimContent(
                    targetState = state,
                    modifier = Modifier.weight(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (it) {
                            InitState.Init -> {
                                LaunchedEffect(Unit) {
                                    viewModel.startApp()
                                    navigateToItem.invoke()
                                }
                            }

                            InitState.Memory ->
                                MemoryPermission {
                                    state =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                            InitState.Notification
                                        else
                                            InitState.Init
                                }

                            InitState.Notification ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    NotificationPermission {
                                        state = InitState.Init
                                    }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.MemoryPermission(onFinish: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
        MemoryPermissionBeforeR(onFinish)
    else
        MemoryPermissionR(onFinish)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ColumnScope.MemoryPermissionBeforeR(onFinish: () -> Unit) {
    val context = LocalContext.current
    val intent = remember {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    // Storage permission state
    val storagePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    when (val status = storagePermissionState.status) {
        is PermissionStatus.Denied -> {
            FromTopToTopAnimContent(targetState = status.shouldShowRationale) {
                when (it) {
                    true ->
                        Text(
                            stringResource(R.string.storage_permission_nonpermission),
                            style = MaterialTheme.typography.subtitle2,
                            textAlign = TextAlign.Center
                        )

                    false ->
                        Text(
                            stringResource(R.string.storage_permission_reason),
                            style = MaterialTheme.typography.subtitle2,
                            textAlign = TextAlign.Center
                        )
                }
            }

            DefaultSpacer()

            FromBottomToBottomAnimContent(targetState = status.shouldShowRationale) {
                when (it) {
                    true ->
                        Button(onClick = { context.startActivity(intent) }) {
                            Text(stringResource(R.string.main_permission_go_to_setting))
                        }

                    false ->
                        Button(onClick = { storagePermissionState.launchPermissionRequest() }) {
                            Text(stringResource(R.string.main_permission_request))
                        }
                }
            }
        }

        PermissionStatus.Granted -> {
            Timber.v("hasPermission")
            onFinish()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun MemoryPermissionR(onFinish: () -> Unit) {
    val context = LocalContext.current
    val intent = remember {
        Intent().apply {
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    var permissionRequire by remember { mutableStateOf(Environment.isExternalStorageManager()) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { permissionRequire = Environment.isExternalStorageManager() }
    )

    if (permissionRequire.not()) {
        Text(stringResource(R.string.storage_permission_reason))
        Spacer(Modifier.height(Dimensions.default))
        Button(onClick = { launcher.launch(intent) }) {
            Text(stringResource(R.string.main_permission_go_to_setting))
        }
    } else {
        onFinish()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ColumnScope.NotificationPermission(onFinish: () -> Unit) {
    val context = LocalContext.current
    val intent = remember {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    // Storage permission state
    val permissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )

    when (val status = permissionState.status) {
        is PermissionStatus.Denied -> {
            FromTopToTopAnimContent(targetState = status.shouldShowRationale) {
                when (it) {
                    true ->
                        Text(
                            stringResource(R.string.notificaton_permission_nonpermission),
                            style = MaterialTheme.typography.subtitle2,
                            textAlign = TextAlign.Center
                        )

                    false ->
                        Text(
                            stringResource(R.string.notificaton_permission_reason),
                            style = MaterialTheme.typography.subtitle2,
                            textAlign = TextAlign.Center
                        )
                }
            }

            DefaultSpacer()

            FromBottomToBottomAnimContent(targetState = status.shouldShowRationale) {
                when (it) {
                    true ->
                        Button(onClick = { context.startActivity(intent) }) {
                            Text(stringResource(R.string.main_permission_go_to_setting))
                        }

                    false ->
                        Button(onClick = { permissionState.launchPermissionRequest() }) {
                            Text(stringResource(R.string.main_permission_request))
                        }
                }
            }
        }

        PermissionStatus.Granted -> onFinish()
    }
}

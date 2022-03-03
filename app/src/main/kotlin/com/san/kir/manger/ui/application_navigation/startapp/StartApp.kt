package com.san.kir.manger.ui.application_navigation.startapp

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.san.kir.manger.R

@Composable
fun StartAppScreen(navigateToItem: () -> Unit,) {
    var action by remember { mutableStateOf(true) }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painterResource(R.mipmap.ic_launcher_foreground),
                "app icon",
                modifier = Modifier.size(300.dp),
            )
            if (action) CircularProgressIndicator()
            Spacer(modifier = Modifier.height(20.dp))

            PermissionPrepare(navigateToItem) { action = it }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionPrepare(
    navigateToItem: () -> Unit,
    viewModel: StartAppViewModel = hiltViewModel(),
    action: (Boolean) -> Unit,
) {

    val state by viewModel.initState.collectAsState()
    // Track if the user doesn't want to see the rationale any more
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    // Storage permission state
    val storagePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    when {
        // permission is granted
        storagePermissionState.hasPermission -> {
            action(true)
            viewModel.startApp()
            if (state == OperationState.SUCCESS) navigateToItem()
        }

        storagePermissionState.shouldShowRationale || !storagePermissionState.permissionRequested -> {
            action(false)
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

        else -> {
            action(false)
            Text(stringResource(R.string.main_permission_nonpermission))
        }
    }
}


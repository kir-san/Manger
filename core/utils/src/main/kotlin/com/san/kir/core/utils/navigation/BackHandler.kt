package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.arkivanov.essenty.backhandler.BackCallback
import com.san.kir.core.utils.coroutines.mainLaunch
import com.san.kir.core.utils.viewModel.LocalComponentContext

@Composable
fun BackHandler(enabled: Boolean, onBack: suspend NavBackHandler.() -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)
    val backHandler = LocalComponentContext.current
    require(backHandler is NavBackHandler) {
        "No ComponentContext was provided via LocalComponentContext or ComponentContext is not NavBackHandler"
    }
    val scope = rememberCoroutineScope()
    val backCallback = remember {
        BackCallback(enabled) {
            scope.mainLaunch { currentOnBack(backHandler) }
        }
    }

    SideEffect {
        backCallback.isEnabled = enabled
    }

    DisposableEffect(backHandler) {
        backHandler.register(backCallback)

        onDispose {
            backHandler.unregister(backCallback)
        }
    }
}

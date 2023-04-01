package com.san.kir.manger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.internet.LocalConnectManager
import com.san.kir.manger.ui.init.InitScreen
import com.san.kir.manger.ui.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var connectManager: ConnectManager

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var isSplash by rememberSaveable { mutableStateOf(true) }

            AnimatedContent(
                targetState = isSplash,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(700),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) with slideOutHorizontally(
                        animationSpec = tween(700),
                        targetOffsetX = { fullWidth -> -fullWidth }
                    )
                }
            ) {
                if (it)
                    InitScreen {
                        Timber.w("Go to library")
                        isSplash = false
                    }
                else
                    CompositionLocalProvider(LocalConnectManager provides connectManager) {
                        MainScreen()
                    }
            }
        }
    }
}



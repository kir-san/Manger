package com.san.kir.manger.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.application_navigation.mainNavGraph
import kotlinx.coroutines.InternalCoroutinesApi

@OptIn(
    InternalCoroutinesApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MangerApp() {

    ProvideWindowInsets {

        val navController = rememberAnimatedNavController()

        AnimatedNavHost(
            navController = navController,
            startDestination = MainNavTarget.StartApp.route,
        ) {
            mainNavGraph(navController)
        }
    }
}

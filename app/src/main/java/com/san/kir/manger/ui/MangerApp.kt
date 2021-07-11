package com.san.kir.manger.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.ui.utils.ProvideWindowInsets
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun MangerApp(close: () -> Unit) {
    val mainNavController = rememberNavController()

    MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
        ProvideWindowInsets {
            NavHost(navController = mainNavController, startDestination = Drawer.route) {
                MAIN_ALL_SCREENS.forEach { screen ->
                    composable(
                        route = screen.route,
                        arguments = screen.arguments,
                        content = { screen.content(mainNavController, close) })
                }
            }
        }
    }
}







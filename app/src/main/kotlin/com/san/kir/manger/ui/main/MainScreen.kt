package com.san.kir.manger.ui.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.san.kir.manger.navigation.MainNavGraph

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val darkTheme by mainViewModel.darkTheme.collectAsState()

    MaterialTheme(colors = if (darkTheme) darkColors() else lightColors()) {
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

        MainNavGraph(rememberAnimatedNavController())
    }
}

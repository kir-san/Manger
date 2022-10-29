package com.san.kir.core.compose

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

object Dimensions {
    val zero = 0.dp
    val smallest = 2.dp
    val quarter = 4.dp
    val smaller = 6.dp
    val half = 8.dp
    val default = 16.dp
    val bigger = 20.dp
    val big = 24.dp

    object Image {
        val small = 36.dp
        val default = 56.dp
        val bigger = 64.dp
        val big = 126.dp

        val storage = DpSize(52.dp, 30.dp)
    }

    object ProgressBar {
        val default = 18.dp
        val toolbar = 40.dp

        val storage = 60.dp

        val strokeSmall = 2.dp
        val strokeDefault = 4.dp
    }

    object Items {
        val height = 48.dp
    }

    val appBarHeight = 56.dp
}

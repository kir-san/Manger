package com.san.kir.core.compose_utils

import androidx.compose.ui.unit.dp

object Dimensions {
    val zero = 0.dp
    val smallest = 2.dp
    val smaller = 4.dp
    val small = 8.dp
    val default = 16.dp
    val big = 24.dp

    object Image {
        val small = 36.dp
        val default = 56.dp
        val big = 126.dp
    }

    object ProgressBar {
        val default = 18.dp
        val toolbar = 40.dp

        val strokeSmall = 2.dp
        val strokeDefault= 4.dp
    }

    val appBarHeight = 56.dp
}

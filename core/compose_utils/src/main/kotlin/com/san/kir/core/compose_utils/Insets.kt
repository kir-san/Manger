package com.san.kir.core.compose_utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues

@Composable
fun systemBarsHorizontalPadding(all: Dp = Dimensions.zero): PaddingValues =
    rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyStart = true, applyEnd = true,
        applyTop = false, applyBottom = false,
        additionalBottom = all,
        additionalEnd = all,
        additionalStart = all,
        additionalTop = all
    )

@Composable
fun systemBarStartPadding(additional: Dp = Dimensions.zero): PaddingValues =
    rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyStart = true, applyEnd = false,
        applyBottom = false, applyTop = false,
        additionalStart = additional,
    )

@Composable
fun systemBarEndPadding(additional: Dp = Dimensions.zero): PaddingValues =
    rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyStart = false, applyEnd = true,
        applyBottom = false, applyTop = false,
        additionalEnd = additional,
    )

@Composable
fun systemBarBottomPadding(additional: Dp = Dimensions.zero): PaddingValues =
    rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyStart = false, applyEnd = false,
        applyBottom = true, applyTop = false,
        additionalBottom = additional,
    )

@Composable
fun systemBarTopPadding(additional: Dp = Dimensions.zero): PaddingValues =
    rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyStart = false, applyEnd = false,
        applyBottom = false, applyTop = true,
        additionalTop = additional,
    )

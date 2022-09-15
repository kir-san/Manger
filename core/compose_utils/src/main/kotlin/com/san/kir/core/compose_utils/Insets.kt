package com.san.kir.core.compose_utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
fun systemBarsHorizontalPadding(all: Dp = Dimensions.zero): PaddingValues =
    WindowInsets.systemBars
        .only(WindowInsetsSides.Horizontal)
        .add(WindowInsets(all, all, all, all))
        .asPaddingValues()

@Composable
fun systemBarStartPadding(additional: Dp = Dimensions.zero): PaddingValues =
    WindowInsets.systemBars
        .only(WindowInsetsSides.Start)
        .add(WindowInsets(left = additional))
        .asPaddingValues()

@Composable
fun systemBarEndPadding(additional: Dp = Dimensions.zero): PaddingValues =
    WindowInsets.systemBars
        .only(WindowInsetsSides.End)
        .add(WindowInsets(right = additional))
        .asPaddingValues()

@Composable
fun systemBarBottomPadding(additional: Dp = Dimensions.zero): PaddingValues =
    WindowInsets.systemBars
        .only(WindowInsetsSides.Bottom)
        .add(WindowInsets(bottom = additional))
        .asPaddingValues()

@Composable
fun systemBarTopPadding(additional: Dp = Dimensions.zero): PaddingValues =
    WindowInsets.systemBars
        .only(WindowInsetsSides.Top)
        .add(WindowInsets(top = additional))
        .asPaddingValues()

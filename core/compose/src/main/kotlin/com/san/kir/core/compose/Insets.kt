package com.san.kir.core.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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

@Composable
private fun horizontalInsets() =
    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
        .add(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))

@Composable
private fun bottomInsets() =
    WindowInsets.displayCutout.only(WindowInsetsSides.Bottom)
        .add(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))

@Composable
private fun startInsets() =
    WindowInsets.displayCutout.only(WindowInsetsSides.Start)
        .add(WindowInsets.systemBars.only(WindowInsetsSides.Start))

@Composable
private fun endInsets() =
    WindowInsets.displayCutout.only(WindowInsetsSides.End)
        .add(WindowInsets.systemBars.only(WindowInsetsSides.End))

fun Modifier.horizontalInsetsPadding() = composed { windowInsetsPadding(horizontalInsets()) }
fun Modifier.startInsetsPadding() = composed { windowInsetsPadding(startInsets()) }
fun Modifier.bottomInsetsPadding() = composed { windowInsetsPadding(bottomInsets()) }
fun Modifier.endInsetsPadding() = composed { windowInsetsPadding(endInsets()) }

@Composable
fun horizontalAndBottomInsetsPadding() = horizontalInsets().add(bottomInsets()).asPaddingValues()
@Composable
fun bottomInsetsPadding() = bottomInsets().asPaddingValues()




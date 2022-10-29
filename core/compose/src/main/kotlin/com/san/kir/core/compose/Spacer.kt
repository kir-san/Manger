package com.san.kir.core.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.SmallestSpacer() {
    Spacer(modifier = Modifier.height(Dimensions.smallest))
}

@Composable
fun ColumnScope.SmallSpacer() {
    Spacer(modifier = Modifier.height(Dimensions.half))
}

@Composable
fun ColumnScope.DefaultSpacer() {
    Spacer(modifier = Modifier.height(Dimensions.default))
}

@Composable
fun RowScope.SmallerSpacer() {
    Spacer(modifier = Modifier.width(Dimensions.quarter))
}

@Composable
fun RowScope.FullWeightSpacer() {
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
fun ColumnScope.FullWeightSpacer() {
    Spacer(modifier = Modifier.weight(1f))
}


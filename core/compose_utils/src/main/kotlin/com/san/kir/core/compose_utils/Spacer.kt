package com.san.kir.core.compose_utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SmallSpacer() {
    Spacer(modifier = Modifier.height(Dimensions.small))
}

@Composable
fun DefaultSpacer() {
    Spacer(modifier = Modifier.height(Dimensions.default))
}

@Composable
fun RowScope.FullWeightSpacer() {
    Spacer(modifier = Modifier.weight(1f))
}
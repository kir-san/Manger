package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable

interface NavComponent<C : NavConfig> {
    @Composable
    fun NavComponentScope.Render()

    val config: C
}

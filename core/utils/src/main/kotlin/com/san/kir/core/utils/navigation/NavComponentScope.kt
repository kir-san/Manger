package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable

interface NavComponentScope : NavBackHandler {

    @Composable
    fun add(navConfig: NavConfig): () -> Unit

    @Composable
    fun <P1> add(function1: (P1) -> NavConfig): (P1) -> Unit

    @Composable
    fun <P1, P2> add(function2: (P1, P2) -> NavConfig): (P1, P2) -> Unit

    @Composable
    fun <P1, P2, P3> add(function3: (P1, P2, P3) -> NavConfig): (P1, P2, P3) -> Unit

    @Composable
    fun replace(navConfig: NavConfig): () -> Unit

    fun simpleAdd(navConfig: NavConfig): () -> Unit

    fun <P1> simpleAdd(function1: (P1) -> NavConfig): (P1) -> Unit
    fun <P1, P2> simpleAdd(function2: (P1, P2) -> NavConfig): (P1, P2) -> Unit
    fun <P1, P2, P3> simpleAdd(function3: (P1, P2, P3) -> NavConfig): (P1, P2, P3) -> Unit
}

package com.san.kir.core.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.mainLaunch(block: suspend CoroutineScope.() -> Unit) =
    launch(mainDispatcher, block = block)

fun CoroutineScope.defaultLaunch(block: suspend CoroutineScope.() -> Unit) =
    launch(defaultDispatcher, block = block)

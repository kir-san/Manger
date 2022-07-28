package com.san.kir.core.utils.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.mainLaunch(block: suspend CoroutineScope.() -> Unit) =
    launch(mainDispatcher, block = block)

fun CoroutineScope.defaultLaunch(block: suspend CoroutineScope.() -> Unit) =
    launch(defaultDispatcher, block = block)

fun CoroutineScope.defaultExcLaunch(
    onFailure: () -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
) = launch(
    defaultDispatcher + CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
        onFailure()
    },
    block = block
)

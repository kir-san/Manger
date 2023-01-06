package com.san.kir.core.utils.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun CoroutineScope.mainLaunch(noinline block: suspend CoroutineScope.() -> Unit) =
    launch(mainDispatcher, block = block)

inline fun CoroutineScope.defaultLaunch(noinline block: suspend CoroutineScope.() -> Unit) =
    launch(defaultDispatcher, block = block)

inline fun CoroutineScope.ioLaunch(noinline block: suspend CoroutineScope.() -> Unit) =
    launch(ioDispatcher, block = block)

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

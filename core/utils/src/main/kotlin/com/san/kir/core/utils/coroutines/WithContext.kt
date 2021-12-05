package com.san.kir.core.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

suspend inline fun <T> withMainContext(noinline block: suspend CoroutineScope.() -> T) =
    withContext(mainDispatcher, block)

suspend inline fun <T> withIoContext(noinline block: suspend CoroutineScope.() -> T) =
    withContext(ioDispatcher, block)

suspend inline fun <T> withDefaultContext(noinline block: suspend CoroutineScope.() -> T) =
    withContext(defaultDispatcher, block)

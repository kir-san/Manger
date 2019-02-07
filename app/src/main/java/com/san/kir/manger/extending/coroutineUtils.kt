package com.san.kir.manger.extending

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun <T> CoroutineScope.asyncCtx(start: CoroutineStart = CoroutineStart.DEFAULT,
                         block: suspend CoroutineScope.() -> T) =
        async(coroutineContext, start, block)

fun CoroutineScope.launchCtx(start: CoroutineStart = CoroutineStart.DEFAULT,
                                block: suspend CoroutineScope.() -> Unit) =
    launch(coroutineContext, start, block)

fun <T> CoroutineScope.asyncUI(start: CoroutineStart = CoroutineStart.DEFAULT,
                                block: suspend CoroutineScope.() -> T) =
    async(Dispatchers.Main, start, block)

fun CoroutineScope.launchUI(start: CoroutineStart = CoroutineStart.DEFAULT,
                            block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.Main, start, block)

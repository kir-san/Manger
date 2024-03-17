package com.san.kir.core.utils

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

suspend fun Lifecycle.repeatOnLifecycle(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit,
) {
    require(state !== Lifecycle.State.INITIALIZED) {
        "repeatOnLifecycle cannot start work with the INITIALIZED lifecycle state."
    }

    if (this.state === Lifecycle.State.DESTROYED) {
        return
    }

    coroutineScope {
        withContext(Dispatchers.Main.immediate) {

            if (this@repeatOnLifecycle.state === Lifecycle.State.DESTROYED) return@withContext

            var launchedJob: Job? = null
            var callbacks: Lifecycle.Callbacks? = null

            try {
                suspendCancellableCoroutine<Unit> { cont ->
                    val mutex = Mutex()
                    callbacks = subscribe(
                        onCreate = {
                            if (state != Lifecycle.State.CREATED) return@subscribe
                            launchedJob = this@coroutineScope.launch {
                                mutex.withLock { coroutineScope { block() } }
                            }
                        },
                        onStart = {
                            if (state != Lifecycle.State.STARTED) return@subscribe
                            launchedJob = this@coroutineScope.launch {
                                mutex.withLock { coroutineScope { block() } }
                            }
                        },
                        onResume = {
                            if (state != Lifecycle.State.RESUMED) return@subscribe
                            launchedJob = this@coroutineScope.launch {
                                mutex.withLock { coroutineScope { block() } }
                            }
                        },
                        onPause = {
                            if (state != Lifecycle.State.RESUMED) return@subscribe
                            launchedJob?.cancel()
                            launchedJob = null
                        },
                        onStop = {
                            if (state != Lifecycle.State.STARTED) return@subscribe
                            launchedJob?.cancel()
                            launchedJob = null
                        },
                        onDestroy = {
                            if (state != Lifecycle.State.CREATED) return@subscribe
                            launchedJob?.cancel()
                            launchedJob = null
                            cont.resume(Unit)
                        },
                    )
                }
            } finally {
                launchedJob?.cancel()
                callbacks?.let { unsubscribe(it) }
            }
        }
    }
}

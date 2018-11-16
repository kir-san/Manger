package com.san.kir.manger.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import kotlin.coroutines.CoroutineContext

class JobContext(private val executor: ExecutorService) : CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = executor.asCoroutineDispatcher() + job

    fun post(run: suspend CoroutineScope.() -> Unit) =
        launch(coroutineContext) {
            run.invoke(this)
        }

    fun close() {
        job.cancel()
    }

    suspend fun join() {
        job.join()
    }
}

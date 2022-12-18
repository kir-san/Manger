package com.san.kir.background.logic.repo

import com.san.kir.data.models.base.BaseTask
import kotlinx.coroutines.flow.Flow

interface BaseWorkerRepository<T : BaseTask<T>> {

    val catalog: Flow<List<T>>

    suspend fun remove(item: T)
}

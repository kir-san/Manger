package com.san.kir.core.utils.viewModel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber

interface EventBus {

    val events: SharedFlow<Event>
    suspend fun sendEvent(event: Event)
}

class EventBusImpl : EventBus {
    override val events = MutableSharedFlow<Event>()

    override suspend fun sendEvent(event: Event) {
        Timber.tag("EventBusImpl").i("EVENT($event)")
        events.emit(event)
    }
}

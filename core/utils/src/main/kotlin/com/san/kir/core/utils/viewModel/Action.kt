package com.san.kir.core.utils.viewModel

interface Action

data class ReturnEvents(val events: List<Event>) : Action

fun ReturnEvents(vararg events: Event): ReturnEvents {
    return ReturnEvents(events.toList())
}

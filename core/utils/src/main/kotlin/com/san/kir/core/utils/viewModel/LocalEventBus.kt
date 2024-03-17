package com.san.kir.core.utils.viewModel

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalEventBus: ProvidableCompositionLocal<EventBus> =
    staticCompositionLocalOf { error("EventBus was not provided") }

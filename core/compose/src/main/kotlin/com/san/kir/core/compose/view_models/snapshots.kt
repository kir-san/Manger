package com.san.kir.core.compose.view_models

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

inline fun <T> ViewModel.snapshot(
    crossinline onEach: suspend (T) -> Unit,
    noinline block: () -> T
) {
    snapshotFlow(block)
        .onEach { onEach(it) }
        .launchIn(viewModelScope)
}

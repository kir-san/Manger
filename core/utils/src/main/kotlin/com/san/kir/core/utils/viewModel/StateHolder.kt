package com.san.kir.core.utils.viewModel

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.flow.StateFlow

interface StateHolder<S : ScreenState> : InstanceKeeper.Instance, EventBus {

    val state: StateFlow<S>

    fun sendAction(action: Action)

}

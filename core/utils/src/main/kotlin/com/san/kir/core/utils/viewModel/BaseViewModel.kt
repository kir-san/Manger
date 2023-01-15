package com.san.kir.core.utils.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultDispatcher
import com.san.kir.core.utils.coroutines.defaultLaunch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

abstract class BaseViewModel<in E : ScreenEvent, out S : ScreenState>
    : ViewModel(), StateHolder<E, S> {

    protected abstract val tempState: Flow<S>
    protected abstract val defaultState: S

    override val state by lazy {
        tempState
            .onEach { Timber.tag("ViewModel").i("NEW STATE $it") }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.Lazily, defaultState)
    }

    protected abstract suspend fun onEvent(event: E)

    override fun sendEvent(event: E) {
        viewModelScope.defaultLaunch {
            Timber.tag("ViewModel").w("ON_EVENT $event")
            onEvent(event)
        }
    }
}

interface StateHolder<in E : ScreenEvent, out S : ScreenState> {
    val state: StateFlow<S>
    fun sendEvent(event: E)
}

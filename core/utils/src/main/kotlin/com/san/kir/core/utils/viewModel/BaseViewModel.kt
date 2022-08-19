package com.san.kir.core.utils.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

abstract class BaseViewModel<in E : ScreenEvent, out S : ScreenState> : ViewModel(), StateHolder<E, S> {

    abstract val tempState: Flow<S>
    abstract val defaultState: S

    override val state by lazy {
        tempState
            .onEach { Timber.i("NEW STATE $it") }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                defaultState
            )
    }

    abstract fun onEvent(event: E): Job

    override fun sendEvent(event: E) {
        Timber.i("ON_EVENT $event")
        onEvent(event)
    }
}

interface StateHolder<in E : ScreenEvent, out S : ScreenState> {
    val state: StateFlow<S>
    fun sendEvent(event: E)
}
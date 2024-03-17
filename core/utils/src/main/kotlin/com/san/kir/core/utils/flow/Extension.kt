package com.san.kir.core.utils.flow

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.san.kir.core.utils.repeatOnLifecycle
import com.san.kir.core.utils.viewModel.LocalComponentContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalComponentContext.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext
) = collectAsStateWithLifecycle(this.value, lifecycleOwner.lifecycle, minActiveState, context)

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
) = collectAsStateWithLifecycle(this.value, lifecycle, minActiveState, context)

@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initial: T,
    lifecycleOwner: LifecycleOwner = LocalComponentContext.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
) = collectAsStateWithLifecycle(initial, lifecycleOwner.lifecycle, minActiveState, context)

@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initial: T,
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
) = produceState(initial, this, lifecycle, minActiveState, context) {
    lifecycle.repeatOnLifecycle(minActiveState) {
        if (context == EmptyCoroutineContext) {
            collect { this@produceState.value = it }
        } else withContext(context) {
            collect { this@produceState.value = it }
        }
    }
}

package com.san.kir.core.utils.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.navigation.NavComponent
import com.san.kir.core.utils.navigation.NavComponentScope
import com.san.kir.core.utils.navigation.NavConfig
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
inline fun <reified VM : StateHolder<*>> stateHolder(
    key: Any = VM::class,
    crossinline creator: @DisallowComposableCalls (EventBus) -> VM,
): VM {
    val eventBus = LocalEventBus.current
    val componentContext = LocalComponentContext.current
    val viewModelKey = key.javaClass.simpleName + key.toString()
    return remember(key) {
        componentContext.instanceKeeper.getOrCreate(viewModelKey) { creator(eventBus) }
    }
}

@Composable
inline fun <reified VM : StateHolder<*>> stateHolder(key: Any = VM::class): VM {
    val componentContext = LocalComponentContext.current
    val viewModelKey = key.javaClass.simpleName + key.toString()
    return remember(key) { componentContext.instanceKeeper.get(viewModelKey) as VM }
}

@Composable
fun StateHolder<*>.rememberSendAction(action: Action): () -> Unit {
    return rememberLambda { sendAction(action) }
}

@Composable
fun StateHolder<*>.rememberSendAction(): (Action) -> Unit =
    rememberLambda { action -> sendAction(action) }

@Composable
fun rememberSendEvent(): (Event) -> Unit {
    val eventBus: EventBus = LocalEventBus.current
    val scope = rememberCoroutineScope()
    return rememberLambda { event -> scope.defaultLaunch { eventBus.sendEvent(event) } }
}

@Composable
fun OnEvent(eventBus: EventBus, handle: (Event) -> Unit) {
    LaunchedEffect(Unit) {
        eventBus.events.onEach { handle(it) }.launchIn(this)
    }
}

@Composable
fun OnGlobalEvent(handle: (Event) -> Unit) {
    OnEvent(LocalEventBus.current, handle)
}

@Composable
fun rememberLambda(action: () -> Unit) = remember { { action() } }

@Composable
fun <P1> rememberLambda(action: (P1) -> Unit) = remember { { value: P1 -> action(value) } }

@Composable
fun <P1, P2> rememberLambda(action: (P1, P2) -> Unit) =
    remember { { p1: P1, p2: P2 -> action(p1, p2) } }

@Composable
fun <P1, P2, P3> rememberLambda(action: (P1, P2, P3) -> Unit) =
    remember { { p1: P1, p2: P2, p3: P3 -> action(p1, p2, p3) } }


@Composable
inline fun <C : NavConfig> NavComponentScope.navCreator(
    crossinline block: @Composable NavComponentScope.(C) -> Unit
): (C) -> NavComponent<C> {
    return { config: C -> navContainer(config, block) }
}

inline fun <C : NavConfig> navContainer(
    config: C,
    crossinline block: @Composable NavComponentScope.(C) -> Unit
): NavComponent<C> {
    return object : NavComponent<C> {
        override val config = config

        @Composable
        override fun NavComponentScope.Render() {
            block(config)
        }
    }
}

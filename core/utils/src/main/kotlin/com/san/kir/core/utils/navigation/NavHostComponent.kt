package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.viewModel.EventBusImpl
import com.san.kir.core.utils.viewModel.LocalComponentContext
import com.san.kir.core.utils.viewModel.LocalEventBus
import com.san.kir.core.utils.viewModel.rememberLambda
import timber.log.Timber

internal class NavHostComponent(
    componentContext: ComponentContext,
    startConfig: NavConfig,
    private val stackAnimation: StackAnimation<NavConfig, NavContainer>
) : ComponentContext by componentContext, NavComponentScope {

    private val navigation = StackNavigation<NavConfig>()
    private val childStack: Value<ChildStack<NavConfig, NavContainer>> =
        childStack(
            source = navigation,
            initialConfiguration = startConfig,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: NavConfig, componentContext: ComponentContext): NavContainer {
        Timber.d("config -> $config")
        return NavContainer(componentContext, ManualDI.navComponent(config))
    }

    @Composable
    override fun replace(navConfig: NavConfig): () -> Unit {
        return rememberLambda { navigation.replaceCurrent(navConfig) }
    }

    override fun simpleAdd(navConfig: NavConfig): () -> Unit {
        return { navigation.push(navConfig) }
    }

    override fun <P1> simpleAdd(function1: (P1) -> NavConfig): (P1) -> Unit {
        return { p1 -> navigation.push(function1(p1)) }
    }

    override fun <P1, P2> simpleAdd(function2: (P1, P2) -> NavConfig): (P1, P2) -> Unit {
        return { p1, p2 -> navigation.push(function2(p1, p2)) }
    }

    override fun <P1, P2, P3> simpleAdd(function3: (P1, P2, P3) -> NavConfig): (P1, P2, P3) -> Unit {
        return { p1, p2, p3 -> navigation.push(function3(p1, p2, p3)) }
    }

    @Composable
    override fun add(navConfig: NavConfig): () -> Unit {
        return rememberLambda { navigation.push(navConfig) }
    }

    @Composable
    override fun <P1> add(function1: (P1) -> NavConfig): (P1) -> Unit {
        return rememberLambda { p1 -> navigation.push(function1(p1)) }
    }

    @Composable
    override fun <P1, P2> add(function2: (P1, P2) -> NavConfig): (P1, P2) -> Unit {
        return rememberLambda { p1, p2 -> navigation.push(function2(p1, p2)) }
    }

    @Composable
    override fun <P1, P2, P3> add(function3: (P1, P2, P3) -> NavConfig): (P1, P2, P3) -> Unit {
        return rememberLambda { p1, p2, p3 -> navigation.push(function3(p1, p2, p3)) }
    }

    override fun backPress() {
        navigation.pop()
    }

    override fun register(callback: BackCallback) {
        backHandler.register(callback)
    }

    override fun unregister(callback: BackCallback) {
        backHandler.unregister(callback)
    }

    @Composable
    fun Show() {
        Children(
            stack = childStack,
            animation = stackAnimation,
        ) { created ->
            CompositionLocalProvider(
                LocalComponentContext provides created.instance.context,
                LocalEventBus provides EventBusImpl()
            ) {
                created.instance.component?.apply {
                    Render()
                }
            }
        }
    }
}

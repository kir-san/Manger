package com.san.kir.core.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.viewModel.EventBusImpl
import com.san.kir.core.utils.viewModel.LocalComponentContext
import com.san.kir.core.utils.viewModel.LocalEventBus
import kotlinx.datetime.Clock.System
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

internal class NavHostComponent(
    componentContext: ComponentContext,
    startConfig: NavConfig,
    serializerModule: SerializersModule,
    private val stackAnimation: StackAnimation<NavConfig, NavContainer>
) : ComponentContext by componentContext, NavComponentScope {

    private var lastPushing = System.now()
    private val navigation = StackNavigation<NavConfig>()

    @OptIn(ExperimentalStateKeeperApi::class, ExperimentalSerializationApi::class)
    private val childStack = childStack(
        source = navigation,
        serializer = NavConfig.serializer(serializerModule),
        initialConfiguration = startConfig,
        handleBackButton = true,
        childFactory = ::createChild,
        // key = "DefaultChildStack" + Random.nextInt(),
    )

    private fun createChild(config: NavConfig, componentContext: ComponentContext): NavContainer {
        Timber.d("config -> $config")
        return NavContainer(
            NavBackComponent(componentContext, navigation),
            ManualDI.navComponent(config)
        )
    }

    private fun push(navConfig: NavConfig) {
        if ((System.now() - lastPushing) < 1.seconds) return
        navigation.pushNew(navConfig)
        lastPushing = System.now()
    }

    @Composable
    override fun replace(navConfig: NavConfig) =
        rememberLambda { navigation.replaceCurrent(navConfig) }

    override fun simpleAdd(navConfig: NavConfig) = { push(navConfig) }

    override fun <P1> simpleAdd(function1: (P1) -> NavConfig): (P1) -> Unit =
        { p1 -> push(function1(p1)) }

    override fun <P1, P2> simpleAdd(function2: (P1, P2) -> NavConfig): (P1, P2) -> Unit =
        { p1, p2 -> push(function2(p1, p2)) }

    override fun <P1, P2, P3> simpleAdd(function3: (P1, P2, P3) -> NavConfig): (P1, P2, P3) -> Unit =
        { p1, p2, p3 -> push(function3(p1, p2, p3)) }

    @Composable
    override fun add(navConfig: NavConfig) = rememberLambda { push(navConfig) }

    @Composable
    override fun <P1> add(function1: (P1) -> NavConfig): (P1) -> Unit =
        rememberLambda { p1 -> push(function1(p1)) }

    @Composable
    override fun <P1, P2> add(function2: (P1, P2) -> NavConfig): (P1, P2) -> Unit =
        rememberLambda { p1, p2 -> push(function2(p1, p2)) }

    @Composable
    override fun <P1, P2, P3> add(function3: (P1, P2, P3) -> NavConfig): (P1, P2, P3) -> Unit {
        return rememberLambda { p1, p2, p3 -> push(function3(p1, p2, p3)) }
    }

    override fun isRegistered(callback: BackCallback) = backHandler.isRegistered(callback)
    override fun backPress() = navigation.pop()
    override fun register(callback: BackCallback) = backHandler.register(callback)
    override fun unregister(callback: BackCallback) = backHandler.unregister(callback)

    @Composable
    fun Show() {
        Children(stack = childStack, animation = stackAnimation) { child ->
            CompositionLocalProvider(
                LocalComponentContext provides child.instance.context,
                LocalEventBus provides EventBusImpl()
            ) {
                child.instance.component?.apply { Render() }
            }
        }
    }
}

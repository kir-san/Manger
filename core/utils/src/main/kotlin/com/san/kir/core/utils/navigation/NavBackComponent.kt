package com.san.kir.core.utils.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.backhandler.BackCallback

class NavBackComponent(
    componentContext: ComponentContext,
    private val navigation: StackNavigation<NavConfig>
) : ComponentContext by componentContext, NavBackHandler {

    override fun backPress() {
        navigation.pop()
    }

    override fun register(callback: BackCallback) {
        backHandler.register(callback)
    }

    override fun unregister(callback: BackCallback) {
        backHandler.unregister(callback)
    }
}

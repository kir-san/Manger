package com.san.kir.core.utils.navigation

import com.arkivanov.essenty.backhandler.BackHandler

interface NavBackHandler : BackHandler {
    fun backPress()
}

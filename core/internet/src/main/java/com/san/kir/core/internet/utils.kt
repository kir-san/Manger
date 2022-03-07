package com.san.kir.core.internet

import androidx.compose.runtime.staticCompositionLocalOf

val LocalConnectManager = staticCompositionLocalOf<ConnectManager> {
    error("CompositionLocal ConnectManager not present")
}

package com.san.kir.library.ui.drawer

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface DrawerEvent : ScreenEvent {
    data class Reorder(val from: Int, val to: Int) : DrawerEvent
}

package com.san.kir.library.ui.drawer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.main.MainMenuItem

@Immutable
internal data class DrawerState(
    val menu: MainMenuItemsState = MainMenuItemsState.Load,
) : ScreenState

@Stable
internal sealed interface MainMenuItemsState {
    @Immutable
    data object Load : MainMenuItemsState
    @Immutable
    data class Ok(val items: List<MenuItem>) : MainMenuItemsState
}

@Immutable
internal data class MenuItem(val item: MainMenuItem, val status: String)

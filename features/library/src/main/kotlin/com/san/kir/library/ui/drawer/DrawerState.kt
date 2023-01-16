package com.san.kir.library.ui.drawer

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.MainMenuItem
import kotlinx.collections.immutable.ImmutableList

internal data class DrawerState(
    val hasEditMenu: Boolean = false,
    val menu: MainMenuItemsState = MainMenuItemsState.Load,
) : ScreenState

internal sealed interface MainMenuItemsState {
    data object Load : MainMenuItemsState
    data class Ok(val items: ImmutableList<MenuItem>) : MainMenuItemsState
}

@Stable
internal data class MenuItem(
    val item: MainMenuItem,
    val status: String,
)

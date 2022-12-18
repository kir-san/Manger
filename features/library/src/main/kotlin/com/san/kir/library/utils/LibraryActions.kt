package com.san.kir.library.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import com.san.kir.core.compose.TopBarActions
import com.san.kir.library.R
import com.san.kir.library.ui.library.LibraryEvent

internal fun libraryActions(
    navigateToOnline: () -> Unit,
    sendEvent: (LibraryEvent) -> Unit,
): @Composable TopBarActions.() -> Unit = {
    MenuIcon(icon = Icons.Default.Add, onClick = navigateToOnline)

    ExpandedMenu {
        MenuText(id = R.string.library_menu_reload) { sendEvent(LibraryEvent.UpdateCurrentCategory) }
        MenuText(id = R.string.library_menu_reload_all) { sendEvent(LibraryEvent.UpdateAll) }
        MenuText(id = R.string.library_menu_update) { sendEvent(LibraryEvent.UpdateApp) }
    }
}

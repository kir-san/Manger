package com.san.kir.core.compose_utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class ExpandedMenuScope internal constructor(
    private val onCloseMenu: () -> Unit,
) {

    @Composable
    fun MenuText(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        DropdownMenuItem(onClick = {
            onClick()
            onCloseMenu()
        }, modifier = modifier) {
            Text(text = text)
        }
    }

    @Composable
    fun MenuText(id: Int, modifier: Modifier = Modifier, onClick: () -> Unit) =
        MenuText(stringResource(id = id), modifier, onClick)

    @Composable
    fun CheckedMenuText(
        id: Int,
        checked: Boolean,
        onClick: () -> Unit,
    ) {
        DropdownMenuItem(onClick) {
            Checkbox(
                checked,
                {
                    onClick()
                    onCloseMenu()
                },
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(stringResource(id))
        }
    }
}

@Composable
fun ExpandedMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onCloseMenu: () -> Unit,
    actions: @Composable ExpandedMenuScope.() -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onCloseMenu, modifier = modifier) {

        ExpandedMenuScope(onCloseMenu).actions()

    }
}
